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
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

class InjectionsCollector {

    private final Collection<Injection> injections = collectInjections();

    Collection<Injection> collectInjections(@NonNull String cls, String mtd) {
        // Parameter 'mtd' may be null in case the caller is interested in /any/ injections related to
        // the given class 'cls'. May be used to decide whether to run the injection process at all or not.
        return injections.stream()
                .filter(injection ->
                        injection.getInClass ().equals(cls)
                    && (injection.getInMethod().equals(mtd) || mtd == null))
                .collect(Collectors.toList());
    }

    private static Collection<Injection> collectInjections() {
        Collection<Injection> injections = new ArrayList<>();
        Reflections reflections = new Reflections(
                "me.darksidecode.keiko.proxy.injector.injections",
                new MethodAnnotationsScanner());

        for (Method method : reflections.getMethodsAnnotatedWith(Inject.class)) {
            validateInjection(method);

            Inject anno = method.getAnnotation(Inject.class);
            injections.add(new Injection(
                    Objects.requireNonNull(anno.inClass().replace('.', '/')),
                    Objects.requireNonNull(anno.inMethod()),
                    Objects.requireNonNull(method.getDeclaringClass().getName().replace('.', '/')),
                    Objects.requireNonNull(method.getName()),
                    Objects.requireNonNull(anno.at())
            ));
        }

        return injections;
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

}
