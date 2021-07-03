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

import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.reflect.MethodCallExtractor;
import me.darksidecode.keiko.reflect.ReflectCached;
import me.darksidecode.keiko.reflect.WrappedObject;

import java.lang.reflect.Method;

public class WrappedBukkitPlayer extends WrappedObject {

    private static final Class<?> playerClass;

    private static final Method getNameMethod;

    static {
        try {
            playerClass = Keiko.INSTANCE.getLoader()
                    .getLoadedClass("org.bukkit.entity.Player");

            getNameMethod = playerClass.getMethod("getName");
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private final ReflectCached<String> name;

    public WrappedBukkitPlayer(Object handle) {
        super(playerClass, handle);
        name = new ReflectCached<>(new MethodCallExtractor<>(getNameMethod, handle));
    }

    public String getName() {
        return name.get();
    }

}
