/*
 * Copyright (C) 2019-2021 German Vekhorev (DarksideCode)
 *
 * This file is part of Keiko Plugin Inspector.
 *
 * Keiko Plugin Inspector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Keiko Plugin Inspector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Keiko Plugin Inspector.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.darksidecode.keiko.proxy.injector;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.injector.injection.Injection;
import me.darksidecode.keiko.proxy.injector.injection.MethodCallInjection;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

class InjectionsCollector {

    private static final String INVALID_IN_METHOD
            = "invalid inMethod value: expected methodName(DescriptorPart1)DescriptorPart2; -- string: ";

    private final Collection<Injection> injections = collectInjections();

    Collection<Injection> collectInjections(@NonNull String cls, String mtdName, String mtdDesc) {
        // Parameter 'mtdName' may be null in case the caller is interested in /any/ injections related to
        // the given class 'cls'. May be used to decide whether to run the injection process at all or not.
        // If 'mtdName' is null, then 'mtdDesc' is treated as null as well.
        String fmtdDesc = mtdName != null ? mtdDesc : null; // ignore explicitly
        
        return injections.stream()
                .filter(injection ->
                        injection.getInClass     ().equals(cls)
                    && (injection.getInMethodName().equals(mtdName ) || mtdName  == null)
                    && (injection.getInMethodDesc().equals(fmtdDesc) || fmtdDesc == null))
                .collect(Collectors.toList());
    }

    long getAppliedInjections() {
        return injections.stream()
                .filter(Injection::isApplied)
                .count();
    }

    long getSkippedInjections() {
        return injections.stream()
                .filter(injection -> !injection.isApplied())
                .count();
    }

    private static Collection<Injection> collectInjections() {
        Collection<Injection> injections = new ArrayList<>();
        Reflections reflections = new Reflections(
                "me.darksidecode.keiko.proxy.injector.injection",
                new TypeAnnotationsScanner(),
                new SubTypesScanner(),
                new MethodAnnotationsScanner());

        collectGenericInjections(reflections, injections);
        collectMethodCallInjections(reflections, injections);

        return injections;
    }

    private static void collectGenericInjections(Reflections reflections, Collection<Injection> injections) {
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(Inject.class)) {
            validateInjection(clazz);

            Inject anno = clazz.getAnnotation(Inject.class);
            String[] inMethod = tokenizeAndValidateInMethod(Objects.requireNonNull(anno.inMethod()));
            Injection injection;

            try {
                Class<? extends Injection> injectionClass = (Class<? extends Injection>) clazz;
                Constructor<? extends Injection> ctor = injectionClass.getConstructor(
                        String.class, // inClass
                        String.class, // inMethodName
                        String.class  // inMethodDesc
                );

                injection = ctor.newInstance(
                        Objects.requireNonNull(anno.inClass().replace('.', '/')),
                        inMethod[0], // method name
                        inMethod[1] // method descriptor
                );
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(
                        "failed to instantiate an Injection (annotated with @Inject): "
                                + clazz.getName() + " - make sure the annotated type " +
                                "provides a constructor identical to the parent constructor", ex);
            }

            injections.add(injection);
        }
    }

    private static void collectMethodCallInjections(Reflections reflections, Collection<Injection> injections) {
        for (Method method : reflections.getMethodsAnnotatedWith(Inject.class)) {
            validateInjection(method);

            Inject anno = method.getAnnotation(Inject.class);
            String[] inMethod = tokenizeAndValidateInMethod(Objects.requireNonNull(anno.inMethod()));

            injections.add(new MethodCallInjection(
                    Objects.requireNonNull(anno.inClass().replace('.', '/')),
                    inMethod[0], // method name
                    inMethod[1], // method descriptor
                    Objects.requireNonNull(method.getDeclaringClass().getName().replace('.', '/')),
                    Objects.requireNonNull(method.getName()),
                    Objects.requireNonNull(anno.at())
            ));
        }
    }

    private static void validateInjection(Class<?> clazz) {
        if (!Injection.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException(
                    "class " + clazz.getName() + " is annotated with @Inject, " +
                            "but does not inherit from " + Injection.class.getName());
    }

    private static void validateInjection(Method method) {
        int access = method.getModifiers();

        if (!Modifier.isPublic(access))
            throw new IllegalArgumentException(
                    "method is annotated with @Inject but is not public - a call to it cannot be injected: "
                            + method.getDeclaringClass().getName() + "#" + method.getName());

        if (!Modifier.isStatic(access))
            throw new IllegalArgumentException(
                    "method is annotated with @Inject but is not static - a call to it cannot be injected: "
                            + method.getDeclaringClass().getName() + "#" + method.getName());

        if (method.getParameterCount() > 0)
            throw new IllegalArgumentException(
                    "method is annotated with @Inject but has parameters - a call to it cannot be injected: "
                            + method.getDeclaringClass().getName() + "#" + method.getName());

        if (method.getReturnType() != void.class) // IMPORTANT: void.class, NOT Void.class
            throw new IllegalArgumentException(
                    "method is annotated with @Inject but has non-void return type - a call to it cannot be injected: "
                            + method.getDeclaringClass().getName() + "#" + method.getName());
    }

    private static String[] tokenizeAndValidateInMethod(String inMethod) {
        String[] parts = inMethod.split("\\(");

        if (parts.length != 2)
            throw new IllegalArgumentException(INVALID_IN_METHOD + inMethod);

        if (parts[1].indexOf(')') == -1)
            throw new IllegalArgumentException(INVALID_IN_METHOD + inMethod);

        if (parts[1].indexOf('L') != -1 && parts[1].indexOf(';') == -1)
            throw new IllegalArgumentException(INVALID_IN_METHOD + inMethod);

        return new String[] {
                parts[0],      // method name
                '(' + parts[1] // method descriptor
        };
    }

}
