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

package me.darksidecode.keiko.tool;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import me.darksidecode.keiko.proxy.Keiko;

abstract class KeikoTool {

    @Getter (AccessLevel.PACKAGE)
    private final String name;

    private final int minArgsLen;

    KeikoTool() {
        this(0);
    }

    KeikoTool(int minArgsLen) {
        if (minArgsLen < 0)
            throw new IllegalArgumentException("minArgsLen cannot be negative");

        this.name = inferToolName();
        this.minArgsLen = minArgsLen;
    }

    private String inferToolName() {
        // Infer the name of this tool from class name.
        StringBuilder builder = new StringBuilder();
        String className = getClass().getSimpleName();
        char[] chars = className.toCharArray();
        boolean firstChar = true;

        // Conversion. Example: "ClearCaches" --> "clear-caches".
        for (char c : chars) {
            if (firstChar) {
                builder.append(Character.toLowerCase(c));
                firstChar = false;
            } else {
                if (Character.isUpperCase(c))
                    builder.append('-');
                builder.append(Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    int executeWithArgs(@NonNull String[] args) {
        if (args.length < minArgsLen) {
            printUsage();
            return 1;
        }

        try {
            return execute(args);
        } catch (Exception ex) {
            Keiko.INSTANCE.getLogger().warningLocalized("tool.err");
            Keiko.INSTANCE.getLogger().error("Unhandled exception during the execution of tool " + name, ex);

            return 1;
        }
    }

    protected abstract int execute(String[] args) throws Exception;

    protected final void printUsage() {
        Keiko.INSTANCE.getLogger().warningLocalized(getI18nPrefix() + "usage");
    }

    protected final String getI18nPrefix() {
        return "tool." + name + ".";
    }

}
