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