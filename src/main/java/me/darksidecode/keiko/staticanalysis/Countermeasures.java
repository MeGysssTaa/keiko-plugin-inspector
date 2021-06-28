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

package me.darksidecode.keiko.staticanalysis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public enum Countermeasures {

    ALWAYS_IGNORE (result -> false),

    ALWAYS_ABORT (result -> true),

    ;

    private static final Countermeasures DEFAULT = ALWAYS_IGNORE;

    @NonNull @Getter
    private final Function<StaticAnalysisResult, Boolean> abortStartupFunc;

    public static Countermeasures fromString(String s) {
        if (s == null)
            return DEFAULT;

        try {
            return valueOf(s.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return DEFAULT;
        }
    }

}
