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

import lombok.Getter;
import lombok.NonNull;

/**
 * Used to provide easier access to classes that we don't have at compile time
 * or at run time with normal access (through non-Keiko class loader(s)).
 */
public abstract class WrappedObject {

    @Getter
    protected final Class<?> type;

    @Getter
    protected final Object handle;

    protected WrappedObject(@NonNull Class<?> type, @NonNull Object handle) {
        Class<?> handleClass = handle.getClass();

        if (!type.isAssignableFrom(handleClass))
            throw new IllegalArgumentException(
                    "invalid handle: expected " + type.getName() + ", but got " + handleClass);

        this.type = type;
        this.handle = handle;
    }

}
