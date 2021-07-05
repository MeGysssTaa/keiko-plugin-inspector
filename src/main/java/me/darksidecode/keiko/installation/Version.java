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

package me.darksidecode.keiko.installation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class Version implements Comparable<Version> {

    public static final int MAX_NUMBER = 1000000000; // = 10^9

    @Getter
    private final int major, minor, patch;

    @Getter @NonNull
    private final Type type;

    @Override
    public int compareTo(@NonNull Version o) {
        if (major > o.major)
            return 1; // this > o
        else if (major == o.major) {
            if (minor > o.minor)
                return 1; // this > o
            else if (minor == o.minor) {
                if (patch > o.patch)
                    return 1; // this > o
                else if (patch == o.patch) {
                    if (type.ordinal() > o.type.ordinal())
                        return 1; // this > o
                    else if (type.ordinal() == o.type.ordinal())
                        return 0; // this = o
                }
            }
        }

        return -1; // this < o
    }

    @Override
    public String toString() {
        String result = major + "." + minor + "." + patch;

        if (type != Type.RELEASE)
            result += "-" + type.name().toLowerCase();

        return result;
    }

    public boolean isNewerThan(@NonNull Version o) {
        return compareTo(o) > 0; // this > o  =>  this was published later than o
    }

    public boolean isOlderThan(@NonNull Version o) {
        return compareTo(o) < 0; // this < o  =>  this was published earlier than o
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch && type == version.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, type);
    }

    public static Version valueOf(@NonNull String s) throws MalformedVersionException {
        Parser parser = new Parser();
        return parser.parse(s);
    }

    public enum Type {
        // Ordered by readiness, ascending.
        DEV,    // absolutely untested development build, created automatically by the CI workflow on each git push
        PRE,    // more or less tested version, but still not recommended for production use (release candidate)
        RELEASE // well-tested version, usually almost 100% ready and safe for production use
    }

    private static class Parser {
        private State currentState = State.PARSING_MAJOR;
        private int major, minor, patch;
        private Type type;

        private Version parse(String s) throws MalformedVersionException {
            int dashIndex = s.indexOf('-');

            // Dot-terminate version string so that the parser does not fail on strings like
            // "1.0.0" with an error like "unexpected end of version string (state: PARSING_PATCH)".
            if (dashIndex == -1)
                s += '.';
            else
                s = new StringBuilder(s).insert(dashIndex, '.').toString();

            char[] chars = s.toCharArray();
            int currentInt = -1;

            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];

                if (Character.isDigit(c)) {
                    int digit = c - '0';

                    if (currentInt == -1) currentInt = 0;
                    currentInt = 10 * currentInt + digit;

                    if (currentInt > MAX_NUMBER)
                        throw new MalformedVersionException(
                                "unexpected digit at char " + i
                                        + " (too many digits in a row - final number is too large): " + s);
                } else if (c == '.') {
                    if (currentInt == -1)
                        throw new MalformedVersionException(
                                "unexpected dot '.' at char " + i
                                        + " (expected an integer): " + s);

                    switch (currentState) {
                        case PARSING_MAJOR:
                            major = currentInt;
                            currentState = State.PARSING_MINOR;
                            break;

                        case PARSING_MINOR:
                            minor = currentInt;
                            currentState = State.PARSING_PATCH;
                            break;

                        case PARSING_PATCH:
                            patch = currentInt;
                            currentState = State.PARSING_TYPE;
                            break;

                        default:
                            throw new MalformedVersionException(
                                    "unexpected dot '.' at char " + i
                                            + " (current state: " + currentState + "): " + s);
                    }

                    currentInt = -1;
                } else if (c == '-') {
                    if (currentState == State.PARSING_TYPE) {
                        if (i == s.length() - 1)
                            throw new MalformedVersionException(
                                    "unexpected dash '-' at char " + i
                                            + " (version string cannot end with this symbol): " + s);

                        String suffix = s.substring(i + 1);

                        try {
                            type = Type.valueOf(suffix.toUpperCase());
                            currentState = State.END;
                            break; // don't parse any more chars
                        } catch (IllegalArgumentException ex) {
                            throw new MalformedVersionException(
                                    "unexpected version type " + suffix
                                            + " (no version type with such name): " + s);
                        }
                    } else
                        throw new MalformedVersionException(
                                "unexpected dash '-' at char " + i
                                        + " (current state: " + currentState + "): " + s);
                } else
                    throw new MalformedVersionException(
                            "unexpected symbol '" + c + "' at char " + i
                                    + " (current state: " + currentState + "): " + s);
            }

            if (currentState == State.PARSING_TYPE) {
                // Infer 'release' type.
                type = Type.RELEASE;
                currentState = State.END;
            }

            if (currentState != State.END)
                throw new MalformedVersionException(
                        "reached end of version string unexpectedly" +
                                " (current state: " + currentState + "): " + s);

            return new Version(major, minor, patch, type);
        }

        private enum State {
            PARSING_MAJOR,
            PARSING_MINOR,
            PARSING_PATCH,
            PARSING_TYPE,
            END
        }
    }

}
