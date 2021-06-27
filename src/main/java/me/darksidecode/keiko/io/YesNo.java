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

package me.darksidecode.keiko.io;

import lombok.AccessLevel;
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

    public static YesNo valueOf(String s) {
        if (s.equalsIgnoreCase(I18n.get("prompts.yes")))
            return YES;
        else if (s.equalsIgnoreCase(I18n.get("prompts.no")))
            return NO;
        else
            return null; // invalid user input
    }

}
