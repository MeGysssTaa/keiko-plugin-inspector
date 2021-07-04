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

package me.darksidecode.keiko.reflect.bukkit;

import lombok.experimental.UtilityClass;
import me.darksidecode.keiko.proxy.Keiko;

import java.lang.reflect.Method;

@UtilityClass
public class WrappedBukkit {

    private static final Class<?> bukkitClass;

    private static final Method getPlayerExactMethod;

    static {
        try {
            bukkitClass = Keiko.INSTANCE.getLoader()
                    .getLoadedClass("org.bukkit.Bukkit");

            getPlayerExactMethod = bukkitClass
                    .getDeclaredMethod("getPlayerExact", String.class);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Object getPlayerExact(String name) {
        try {
            // returns handle: org.bukkit.entity.Player
            return getPlayerExactMethod.invoke(null, name);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("fatal reflection failure", ex);
        }
    }

}
