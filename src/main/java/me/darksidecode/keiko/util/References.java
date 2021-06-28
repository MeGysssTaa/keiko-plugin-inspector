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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.regex.Pattern;

public final class References implements Opcodes {

    /**
     * Pattern used to name methods in most programs and obfuscators.
     * Includes alphabet (case-insensetive), digits, and underscores.
     */
    private static final Pattern COMMON_METHOD_NAME_PATTERN
            = Pattern.compile("^[a-zA-Z0-9_]+$");

    private References() {}

    public static boolean isPrivate(MethodNode mtdNode) {
        return (mtdNode.access & ACC_PRIVATE) != 0;
    }

    public static boolean isStatic(MethodNode mtdNode) {
        return (mtdNode.access & ACC_STATIC) != 0;
    }

    public static boolean isBridge(MethodNode mtdNode) {
        return (mtdNode.access & ACC_BRIDGE) != 0;
    }

    public static boolean isSynthetic(MethodNode mtdNode) {
        return (mtdNode.access & ACC_SYNTHETIC) != 0;
    }

    public static boolean isDeprecated(MethodNode mtdNode) {
        return (mtdNode.access & ACC_DEPRECATED) != 0;
    }

    public static boolean isNamedSuspiciously(MethodNode mtdNode) {
        return !COMMON_METHOD_NAME_PATTERN.matcher(mtdNode.name).matches();
    }

}
