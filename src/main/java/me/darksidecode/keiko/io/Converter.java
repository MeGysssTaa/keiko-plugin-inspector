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

package me.darksidecode.keiko.io;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class Converter<T> {

    private final Method valueOf;

    Converter(Class<? extends T> targetType) {
        try {
            valueOf = targetType.getMethod("valueOf", String.class);
            int mod = valueOf.getModifiers();

            if (!Modifier.isPublic(mod) || !Modifier.isStatic(mod))
                throw new NoSuchMethodException("the method exists, but is not public or is not static");
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("cannot create a Converter for type "
                    + targetType.getName() + ": missing public static method 'valueOf(java.lang.String)'", ex);
        }
    }

    T convert(String s) {
        try {
            return (T) valueOf.invoke(null, s);
        } catch (Exception ex) {
            return null; // invalid user input
        }
    }

}
