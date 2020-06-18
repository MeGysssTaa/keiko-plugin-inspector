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

    public static String transformedClassName(Class clazz) {
        return clazz.getName().replace(".", "/");
    }

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
