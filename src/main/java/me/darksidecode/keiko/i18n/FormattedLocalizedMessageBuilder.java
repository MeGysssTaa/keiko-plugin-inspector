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

package me.darksidecode.keiko.i18n;

import lombok.NonNull;
import me.darksidecode.kantanj.formatting.ConditionalFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Needed because kantanj's ConditionalFormatter only supports one conditional
 * block ("text 'conditional block' text") per string, and we may sometimes need more.
 * So we basically divide our messages in multiple parts, with each part containing no
 * more than one conditional block, and then just join them together.
 */
class FormattedLocalizedMessageBuilder {

    private final List<ConditionalFormatter> formatters = new ArrayList<>();

    FormattedLocalizedMessageBuilder(@NonNull String message) {
        if (message.trim().isEmpty())
            throw new IllegalArgumentException("message must not be empty or effectively empty");

        char[] chars = message.toCharArray();
        StringBuilder partBuilder = new StringBuilder();
        int quotes = 0;

        for (char c : chars) {
            partBuilder.append(c);

            if (c == '\'' && ++quotes == 2) {
                formatters.add(new ConditionalFormatter(partBuilder.toString()));
                partBuilder = new StringBuilder();
                quotes = 0;
            }
        }

        if (quotes != 0)
            throw new IllegalStateException("unterminated quote in message");

        if (partBuilder.length() > 0) // remainder
            formatters.add(new ConditionalFormatter(partBuilder.toString()));
    }

    String build(Object... args) {
        Object[] targetArgs = args == null ? new Object[0] : args;
        StringBuilder result = new StringBuilder();
        formatters.forEach(formatter -> result.append(formatter.format(targetArgs)));
        return result.toString();
    }

}
