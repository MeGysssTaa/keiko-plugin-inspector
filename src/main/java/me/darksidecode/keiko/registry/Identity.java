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

package me.darksidecode.keiko.registry;

import lombok.Getter;
import lombok.NonNull;
import me.darksidecode.keiko.util.StringUtils;

public class Identity {

    @Getter
    private final String filePath, pluginName, className, methodName;

    private final boolean filterBase;

    public Identity(String filePath, String pluginName, String className, String methodName) {
        this(false, filePath, pluginName, className, methodName);
    }

    Identity(boolean filterBase, String filePath, String pluginName, String className, String methodName) {
        this.filterBase = filterBase;
        this.filePath   = get(filterBase, filePath,   "filePath"  );
        this.pluginName = get(filterBase, pluginName, "pluginName");
        this.className  = replaceIfNotNull(
                          get(filterBase, className,  "className" ), '/', '.');
        this.methodName = get(filterBase, methodName, "methodName");
    }

    private static String get(boolean allowNull, String val, @NonNull String what) {
        if (val == null) {
            if (allowNull)
                return null;
            else
                throw new NullPointerException(what + " cannot be null");
        } else
            return StringUtils.basicReplacements(val);
    }

    private static String replaceIfNotNull(String s, char oldChar, char newChar) {
        return s == null ? null : s.replace(oldChar, newChar);
    }

    @Override
    public String toString() {
        return "[Plugin: " + pluginName + ", Source: " + className + "#" + methodName + "]";
    }

    boolean matches(@NonNull Identity other) {
        if (!filterBase)
            throw new IllegalStateException(
                    "match is only allowed to be called on filter base Identity classes");

        if (other.filterBase)
            throw new IllegalStateException(
                    "cannot match a filter base Identity class");

        return     (filePath   == null /* match any */ || StringUtils.matchWildcards(other.filePath,   filePath  ))
                && (pluginName == null /* match any */ || StringUtils.matchWildcards(other.pluginName, pluginName))
                && (className  == null /* match any */ || StringUtils.matchWildcards(other.className,  className ))
                && (methodName == null /* match any */ || StringUtils.matchWildcards(other.methodName, methodName));
    }

}
