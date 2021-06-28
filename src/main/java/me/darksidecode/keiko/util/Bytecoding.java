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

import me.darksidecode.keiko.KeikoPluginInspector;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class Bytecoding {

    private Bytecoding() {}

    public static Map<String, ClassNode> loadClasses(File jarFile) {
        try {
            Map<String, ClassNode> classes = new HashMap<>();
            JarFile jar = new JarFile(jarFile);
            Stream<JarEntry> str = jar.stream();

            str.forEach(entry -> readJarEntry(jar, entry, classes));
            jar.close();

            return classes;
        } catch (Exception ex) {
            throw new RuntimeException("failed to load " +
                    "classes from JAR file: " + jarFile.getAbsolutePath(), ex);
        }
    }

    private static void readJarEntry(JarFile jar, JarEntry entry, Map<String, ClassNode> classes) {
        String name = entry.getName();

        try (InputStream input = jar.getInputStream(entry)) {
            if (name.endsWith(".class")) {
                byte[] bytes = IOUtils.toByteArray(input);

                if (bytes.length > 4) {
                    String cafebabe = String.format(
                            "%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);

                    // Validate Java class (should start with "magic word" CAFEBABE (hexadecimal)).
                    if (cafebabe.equalsIgnoreCase("CAFEBABE")) {
                        try {
                            ClassNode clsNode = getNode(bytes);

                            if ((clsNode != null)
                                    && ((clsNode.name.equals("java/lang/Object"))
                                    || (clsNode.superName != null)))
                                classes.put(clsNode.name, clsNode);
                        } catch (Exception ex) {
                            throw new RuntimeException("failed to read a Java class", ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("failed to read a Java entry", ex);
        }
    }

    private static ClassNode getNode(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode clsNode = new ClassNode();

        try {
            reader.accept(clsNode, ClassReader.EXPAND_FRAMES);
        } catch (Exception ex) {
            try {
                // Try harder...
                KeikoPluginInspector.debug("Failed to get node from bytes with EXPAND_FRAMES. " +
                        "A retry attempt will be performed with SKIP_FRAMES|SKIP_DEBUG. Cause: " + ex.toString());
                reader.accept(clsNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            } catch (Exception fatal) {
                KeikoPluginInspector.warn("Skipping class %s (v%d): errors decompiling: (1) %s, (2) %s",
                        clsNode.name, clsNode.version, ex.toString(), fatal.toString());

                // Don't rethrow so that we don't skip the whole JAR,
                // file but only this particular "broken" class.
                return null;
            }
        }

        return clsNode;
    }

}
