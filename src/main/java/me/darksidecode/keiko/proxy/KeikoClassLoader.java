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

package me.darksidecode.keiko.proxy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import me.darksidecode.jminima.disassembling.SimpleJavaDisassembler;
import me.darksidecode.jminima.phase.PhaseExecutionException;
import me.darksidecode.jminima.phase.PhaseExecutionWatcher;
import me.darksidecode.jminima.phase.basic.EmitArbitraryValuePhase;
import me.darksidecode.jminima.workflow.Workflow;
import me.darksidecode.jminima.workflow.WorkflowExecutionResult;
import me.darksidecode.keiko.proxy.injector.InjectionException;
import me.darksidecode.keiko.proxy.injector.Injector;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class KeikoClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    @Getter (AccessLevel.PACKAGE)
    private final String bootstrapClassName;

    private final URL url;

    private final JarFile jar;

    private final Manifest manifest;

    private final Workflow workflow;

    private Injector injector;

    @Getter (AccessLevel.PACKAGE)
    private LoadClassesPhase.Result loadResult;

    KeikoClassLoader(@NonNull File proxiedExecutable) throws IOException {
        super(new URL[] { proxiedExecutable.toURI().toURL() });

        this.url = proxiedExecutable.toURI().toURL();
        this.jar = new JarFile(proxiedExecutable);
        this.manifest = jar.getManifest();
        this.bootstrapClassName = manifest == null ? null
                : manifest.getMainAttributes().getValue("Main-Class");

        // Load classes and inject our code.
        // Don't close this Workflow (nor use it in a try-with-resources) because it will close the
        // JarFile as well. We don't want this - the JarFile is still used later (in loadClassFromJar).
        workflow = new Workflow()
                .phase(new EmitArbitraryValuePhase<>(jar))
                .phase(new DetectMinecraftVersionPhase()
                        .watcher(new PhaseExecutionWatcher<String>()
                                .doAfterExecution((val, err) -> {
                                    // It is important that we only create Injector afted detecting the proxied
                                    // Minecraft server version so that this version is already known in the
                                    // LoadClassesPhase. If we created Injector earlier, then the PlaceholderApplicator
                                    // (in InjectionsCollector) would be unable to replace placeholders like
                                    // "{nms_version}" properly (because the value has not been set yet).
                                    Keiko.INSTANCE.getEnv().setNmsVersion(val);
                                    injector = new Injector(jar, new SimpleJavaDisassembler(jar));
                                })
                                .doOnWatcherError(t -> {
                                    // Fatal error in code (for example, an invalid @Inject annotation).
                                    Keiko.INSTANCE.getLogger().error("Failed to create injector", t);
                                    System.exit(1);
                                })
                        ))
                .phase(new LoadClassesPhase(this)
                        .watcher(new PhaseExecutionWatcher<LoadClassesPhase.Result>()
                                .doAfterExecution((val, err) -> loadResult = val)
                        ));

        WorkflowExecutionResult result = workflow.executeAll();

        if (result == WorkflowExecutionResult.FATAL_FAILURE) {
            int errNum = 0;

            for (PhaseExecutionException ex : workflow.getAllErrorsChronological())
                Keiko.INSTANCE.getLogger().error("JMinima error #%d", ++errNum, ex);

            throw new IllegalStateException("fatal class loader failure");
        }

        // Print some stats regarding injection. (Non-localized: this is a very low-level debug.)
        long appliedInjections = injector.getAppliedInjections();
        long skippedInjections = injector.getSkippedInjections();

        Keiko.INSTANCE.getLogger().debug("Injections applied: %d/%d (%d skipped)",
                appliedInjections, appliedInjections + skippedInjections, skippedInjections);
    }

    public Class<?> getLoadedClass(@NonNull String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null)
            throw new ClassNotFoundException(
                    name + " (Is the name correct? Has this class been loaded before?)");

        return result;
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

            try {
                classBytes = injector.inject(entry);
            } catch (InjectionException ex) {
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
        workflow.close();

        try {
            super.close();
        } finally {
            jar.close();
        }
    }

}
