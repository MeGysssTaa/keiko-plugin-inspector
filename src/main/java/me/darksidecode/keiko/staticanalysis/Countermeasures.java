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

package me.darksidecode.keiko.staticanalysis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.proxy.Keiko;

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
        if (s == null) {
            Keiko.INSTANCE.getLogger().error("Countermeasures string is null");
            return DEFAULT;
        }

        try {
            return valueOf(s.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            Keiko.INSTANCE.getLogger().error("Countermeasures string is invalid: '%s'", s);
            return DEFAULT;
        }
    }

}
