/*
 * Copyright 2020 DarksideCode
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

import java.util.regex.Pattern;

public final class StringUtils {

    private StringUtils() {}

    private static final String NO_WILDCARDS_PREFIX = "NO_WILDCARDS :: ";
    private static final String ESCAPED_DOT         = Pattern.quote(".");

    public static boolean matchWildcards(String text, String pattern) {
        if (pattern.startsWith(NO_WILDCARDS_PREFIX))
            return text.equals(pattern.replace(NO_WILDCARDS_PREFIX, ""));

        return text.matches(pattern
                // Allow ordinary dots "." in rules' arguments without forcing the user
                // to escape it manually, because ordinary dots "." are very often used
                // in rules' arguments.
                .replace(".", ESCAPED_DOT)
                // Regex (simplified, i.e. dot-reduced). These characters are not
                // as widely used as dots ".", so users have to escape them manually
                // if they need to.
                .replace("*", ".*")
                .replace("+", ".+")
                .replace("?", ".?")
        );
    }

    public static String replacePortByName(String s) {
        switch (s.toUpperCase()) {
            default:
                return s; // no special port name

            case "HTTP":
                return "80";

            case "HTTP_ALT":
                return "8080";

            case "HTTPS":
                return "443";

            case "FTP":
                return "21";

            case "SFTP":
            case "SSH":
                return "22";
        }
    }

}
