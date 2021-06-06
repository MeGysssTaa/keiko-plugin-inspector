/*
 * Copyright 2021 German Vekhorev (DarksideCode)
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

package me.darksidecode.keiko.proxy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import me.darksidecode.jminima.phase.basic.EmitArbitraryValuePhase;
import me.darksidecode.jminima.workflow.Workflow;
import me.darksidecode.jminima.workflow.WorkflowExecutionResult;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

class KeikoClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    @Getter (AccessLevel.PACKAGE)
    private final String bootstrapClassName;

    private final URL url;

    private final JarFile jar;

    private final Manifest manifest;

    @Getter (AccessLevel.PACKAGE)
    private LoadClassesPhase.Result loadResult;

    KeikoClassLoader(@NonNull File proxiedExecutable) throws IOException {
        super(new URL[] { proxiedExecutable.toURI().toURL() });

        this.url = proxiedExecutable.toURI().toURL();
        this.jar = new JarFile(proxiedExecutable);
        this.manifest = jar.getManifest();
        this.bootstrapClassName = manifest == null ? null
                : manifest.getMainAttributes().getValue("Main-Class");

        Workflow workflow = new Workflow()
                .phase(new EmitArbitraryValuePhase<>(() -> jar))
                .phase(new LoadClassesPhase(this)
                        .afterExecution((val, err) -> loadResult = val));

        WorkflowExecutionResult result = workflow.executeAll();

        if (result == WorkflowExecutionResult.FATAL_FAILURE)
            throw new IllegalStateException("fatal class loader failure");
    }

    @Override
    public Class<?> findClass(@NonNull String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null)
            result = loadClassFromJar(name);

        return result;
    }

    private Class<?> loadClassFromJar(String name) throws ClassNotFoundException {
        Class<?> result = null;
        String path = name.replace('.', '/') + ".class";
        JarEntry entry = jar.getJarEntry(path);

        if (entry != null) {
            byte[] classBytes;

            try (InputStream stream = jar.getInputStream(entry)) {
                classBytes = IOUtils.toByteArray(stream);
            } catch (IOException ex) {
                throw new ClassNotFoundException(name, ex);
            }

            int lastDotIndex = name.lastIndexOf('.');

            if (lastDotIndex != -1) {
                String packageName = name.substring(0, lastDotIndex);

                if (getPackage(packageName) == null) {
                    try {
                        if (manifest != null)
                            definePackage(packageName, manifest, url);
                        else
                            definePackage(packageName, null, null,
                                    null, null, null, null, null);
                    } catch (IllegalArgumentException ex) {
                        throw new ClassNotFoundException("package not found: " + packageName, ex);
                    }
                }
            }

            CodeSigner[] signers = entry.getCodeSigners();
            CodeSource source = new CodeSource(url, signers);
            result = defineClass(name, classBytes, 0, classBytes.length, source);
        }

        if (result == null)
            result = super.findClass(name);

        classes.put(name, result);

        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

}
