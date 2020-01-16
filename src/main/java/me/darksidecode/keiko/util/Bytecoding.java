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
                String cafebabe = String.format(
                        "%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);

                // Validate Java class (should start with "magic word" CAFEBABE (hexadecimal)).
                if (cafebabe.toUpperCase().equals("CAFEBABE")) {
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
                throw new RuntimeException("failed to get node " +
                        "from bytes, previous error: " + ex.toString(), fatal);
            }
        }

        return clsNode;
    }

}
