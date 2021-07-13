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

package me.darksidecode.keiko.util;

import lombok.NonNull;
import me.darksidecode.kantanj.formatting.Hash;
import me.darksidecode.keiko.proxy.Keiko;

import java.io.File;
import java.util.regex.Pattern;

public final class StringUtils {

    private StringUtils() {}

    private static final String NO_WILDCARDS_PREFIX = "NO_WILDCARDS :: ";
    private static final String ESCAPED_DOT         = Pattern.quote(".");

    public static String orEmpty(String s) {
        return s == null ? "" : s;
    }

    public static String basicReplacements(@NonNull String s) {
        if (Keiko.INSTANCE.getLaunchState() != Keiko.LaunchState.NOT_LAUNCHED) // might be called, e.g., from tests (some <clinit>s)
            s = s.replace("{keiko_folder}",   Keiko.INSTANCE.getEnv().getWorkDir   ().getAbsolutePath())
                 .replace("{server_folder}",  Keiko.INSTANCE.getEnv().getServerDir ().getAbsolutePath());

        return s.replace("{java_folder}", System.getProperty("java.home"))
                .replace('\\', '/'); // better Windows compatibility (THIS REPLACE MUST BE MADE LAST!)
    }

    public static String pad(@NonNull String what, char padWith, int finalLength) {
        if (what.length() >= finalLength)
            return what;

        StringBuilder result = new StringBuilder(what);
        int charsToAdd = finalLength - what.length();

        for (int i = 0; i < charsToAdd; i++)
            result.append(padWith);

        return result.toString();
    }

    public static String sha512(@NonNull File file) {
        return Hash.SHA512.checksumString(file).toLowerCase();
    }

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
