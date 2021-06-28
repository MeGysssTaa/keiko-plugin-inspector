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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.i18n.I18n;

@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class YesNo {

    public static final YesNo YES = new YesNo(true );
    public static final YesNo NO  = new YesNo(false);

    private final boolean value;

    public boolean toBoolean() {
        return value;
    }

    @SuppressWarnings ("unused") // called reflectively from Converter
    public static YesNo valueOf(@NonNull String s) {
        // Use localized words for "yes" and "no" for better user experience.
        if (s.equalsIgnoreCase(I18n.get("prompts.yes")))
            return YES;
        else if (s.equalsIgnoreCase(I18n.get("prompts.no")))
            return NO;
        else
            return null; // invalid user input
    }

}
