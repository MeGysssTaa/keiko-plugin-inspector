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

import lombok.RequiredArgsConstructor;

/**
 * Used to cache reflection calls to methods whose return value never changes,
 * and that do not accept any input arguments (have no method parameters).
 */
@RequiredArgsConstructor
public class ReflectCached<T> {

    private final ReflectValueExtractor<T> valueExtractor;

    private T value;

    public T get() {
        if (value == null) {
            try {
                value = valueExtractor.extract();
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("fatal reflection failure", ex);
            }
        }

        return value;
    }

}
