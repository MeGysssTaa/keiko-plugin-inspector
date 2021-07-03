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

import lombok.Getter;

public class MethodParam<T> {

    @Getter
    private final T value; // may be null

    @Getter
    private final Class<T> type; // guaranteed non-null if value is non-null; guaranteed null if value is null

    public MethodParam(T value) {
        this.value = value;
        this.type = value != null ? (Class<T>) value.getClass() : null;
    }

    public boolean isPresent() {
        return value != null;
    }

    public static <T> MethodParam<T> wrap(T value) {
        return new MethodParam<>(value);
    }

}
