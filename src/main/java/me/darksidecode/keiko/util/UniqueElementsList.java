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

package me.darksidecode.keiko.util;

import java.util.ArrayList;

public class UniqueElementsList<T> extends ArrayList<T> {

    private static final long serialVersionUID = 3582559766004872759L;

    private final boolean silent;

    public UniqueElementsList() {
        this(true);
    }

    public UniqueElementsList(boolean silent) {
        this.silent = silent;
    }

    @Override
    public boolean add(T t) {
        if (!contains(t))
            return super.add(t);
        else if (silent)
            return false;
        else
            throw new IllegalStateException("duplicate element");
    }

    @Override
    public UniqueElementsList<T> clone() {
        return (UniqueElementsList<T>) super.clone();
    }

}
