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

package me.darksidecode.keiko.registry;

import lombok.Getter;
import lombok.NonNull;

public class IdentityFilter {

    public static final String ERR_PREFIX = "identityFilter.err.";

    private final Identity filter;

    @Getter
    private final String errorI18nKey;

    public IdentityFilter(@NonNull String s) {
        String[] tokens = s.trim().split("=");
        Identity filter = null;
        String errorI18nKey = null;

        if (tokens.length == 2) {
            String filterType = tokens[0].trim().toUpperCase();
            String matcher = tokens[1].trim();

            switch (filterType) {
                case "FILE":
                    filter = new Identity(
                            true, matcher, null, null, null);

                    break;

                case "PLUGIN":
                    filter = new Identity(
                            true, null, matcher, null, null);

                    break;

                case "SOURCE":
                    String[] sourceMatcher = matcher.split("#");
                    String classMatcher = sourceMatcher[0].trim();
                    String methodMatcher;

                    if (sourceMatcher.length == 2)
                        methodMatcher = sourceMatcher[1].trim();
                    else if (sourceMatcher.length == 1)
                        methodMatcher = null;
                    else {
                        errorI18nKey = ERR_PREFIX + "invalidSyntax";
                        break;
                    }

                    filter = new Identity(
                            true, null, null, classMatcher, methodMatcher);

                    break;

                default:
                    errorI18nKey = ERR_PREFIX + "invalidFilterType";
                    break;
            }
        } else
            errorI18nKey = ERR_PREFIX + "invalidSyntax";

        this.filter = filter;
        this.errorI18nKey = errorI18nKey;
    }

    public boolean matches(@NonNull Identity other) {
        if (filter != null)
            return filter.matches(other);
        else
            throw new UnsupportedOperationException("invalid filter (error key: " + errorI18nKey + ")");
    }

}