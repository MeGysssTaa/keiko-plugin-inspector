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
