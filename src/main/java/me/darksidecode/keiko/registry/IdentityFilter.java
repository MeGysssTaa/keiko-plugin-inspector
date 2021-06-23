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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class IdentityFilter {

    public static final String ERR_PREFIX = "identityFilter.err.";

    private final Identity filter;

    @Getter
    private final String errorI18nKey;

    private IdentityFilter(Identity filter) {
        this(filter, null);
    }

    private IdentityFilter(String errorI18nKey) {
        this(null, errorI18nKey);
    }

    public boolean matches(@NonNull Identity other) {
        if (filter != null)
            return filter.matches(other);
        else
            throw new UnsupportedOperationException("invalid filter (error key: " + errorI18nKey + ")");
    }

    public static IdentityFilter valueOf(@NonNull String s) {
        String[] tokens = s.trim().split("=");

        if (tokens.length != 2)
            return new IdentityFilter(ERR_PREFIX + "invalidSyntax");

        String filterType = tokens[0].trim().toUpperCase();
        String matcher = tokens[1].trim();

        switch (filterType) {
            case "FILE":
                return new IdentityFilter(new Identity(
                        true, matcher, null, null, null));

            case "PLUGIN":
                return new IdentityFilter(new Identity(
                        true, null, matcher, null, null));

            case "SOURCE":
                String[] sourceMatcher = matcher.split("#");
                String classMatcher = sourceMatcher[0].trim();
                String methodMatcher;

                if (sourceMatcher.length == 2)
                    methodMatcher = sourceMatcher[1].trim();
                else if (sourceMatcher.length == 1)
                    methodMatcher = null;
                else
                    return new IdentityFilter(ERR_PREFIX + "invalidSyntax");

                return new IdentityFilter(new Identity(
                        true, null, null, classMatcher, methodMatcher));

            default:
                return new IdentityFilter(ERR_PREFIX + "invalidFilterType");
        }
    }

}
