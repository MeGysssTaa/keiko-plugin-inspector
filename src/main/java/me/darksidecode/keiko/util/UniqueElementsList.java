/*
 * Copyright 2021 German Vekhorev (DarksideCode)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
