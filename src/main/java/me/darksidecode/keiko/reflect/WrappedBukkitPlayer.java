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

package me.darksidecode.keiko.reflect;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.Keiko;

import java.lang.reflect.Method;

public class WrappedBukkitPlayer {

    private static final Class<?> bukkitPlayerClass;

    private static final Method getName;

    static {
        try {
            bukkitPlayerClass = Keiko.INSTANCE.getLoader()
                    .getLoadedClass("org.bukkit.entity.Player");

            getName = bukkitPlayerClass.getMethod("getName");
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private final Object handle;

    private final ReflectCached<String> name;

    public WrappedBukkitPlayer(@NonNull Object handle) {
        Class<?> handleClass = handle.getClass();

        if (!bukkitPlayerClass.isAssignableFrom(handleClass))
            throw new IllegalArgumentException(
                    "invalid handle: expected " + bukkitPlayerClass.getName()
                            + ", but got " + handleClass);

        this.handle = handle;
        this.name = new ReflectCached<>(getName, handle);
    }

    public String getName() {
        return name.get();
    }

}
