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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If put on a type that inherits from abstract class Injection, indicates that this class's
 * overriden apply(...) method should be called on a ClassNode+MethodNode pair that satisfies
 * the requirements provided with annotation parameters. Enables creation of arbitrary class
 * bytecode transformers that can remove or add any bytecode in any place.
 *
 * If put on a public static method without parameters, indicates that a call to this method
 * should be inserted at the given position of a ClassNode+MethodNode pair that satisfies the
 * requirements provided with annotation parameters. Makes it very easy to append arbitrary
 * Java code at a certain position of a method, without having to hussle with bytecode/ASM.
 */
@Target ({ ElementType.TYPE, ElementType.METHOD })
@Retention (RetentionPolicy.RUNTIME)
public @interface Inject {

    /**
     * Fully qualified name of the class where the injection should be applied.
     * Package separator must be either '.' or '/' (both will work)..
     */
    String inClass();

    /**
     * Name and descriptor of the method of the class 'inClass()' where the injection should be applied,
     * without any separators. For example, if a method is defined as "public static int foo()", then the
     * 'inMethod()' parameter should look like "foo()I" (see JVM method descriptor specification for details).
     */
    String inMethod();

    /**
     * Relative insertion position (only used for MethodCallInjection).
     * Cannot be UNUSED for MethodCallInjection, but may be anything for other Injections.
     *
     * @see me.darksidecode.keiko.proxy.injector.injection.MethodCallInjection
     */
    Position at() default Position.UNUSED;

    /**
     * Only used for MethodCallInjection.
     * Probably should be refactored.
     */
    enum Position {
        UNUSED, // for everything other than MethodCallInjection
        BEGINNING,
        END
    }

}
