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

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

public final class Keiko {

    private static final String LOGO =
            "\n\n" +
            "     _  __      _  _          \n" +
            "    | |/ / ___ (_)| | __ ___  \n" +
            "    | ' / / _ \\| || |/ // _ \\ \n" +
            "    | . \\|  __/| ||   <| (_) |\n" +
            "    |_|\\_\\\\___||_||_|\\_\\\\___/ \n" +
            "\n";

    public static final Keiko INSTANCE = new Keiko();

    private volatile boolean started;

    @Getter
    private final BuildProperties buildProperties;

    @Getter
    private final File workDir;

    @Getter
    private final KeikoLogger logger;

    @SuppressWarnings ("UseOfSystemOutOrSystemErr")
    private Keiko() {
        checkEnvironment();

        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("build.properties")) {
            Properties properties = new Properties();
            properties.load(stream);
            buildProperties = new BuildProperties(properties);
        } catch (IOException ex) {
            throw new RuntimeException("failed to load build.properties", ex);
        }

        System.out.println(LOGO);
        System.out.println("      --  " + buildProperties.getVersion());
        System.out.println("      --  " + buildProperties.getTimestamp());
        System.out.println("\n\n");

        workDir = new File(KeikoProperties.workDirPath);
        logger = new KeikoLogger(KeikoProperties.debug, new File(workDir, "logs"));

        if (KeikoProperties.debug)
            logger.debug("Note: debug is enabled. Run with \"-Dkeiko.debug=false\" to disable");
        else
            logger.info("Note: debug is disabled. Run with \"-Dkeiko.debug=true\" to enable");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            INSTANCE.getLogger().error("Please specify the JAR file for Keiko to proxy.");
            INSTANCE.getLogger().error("For details, see the installations instructins at:");
            INSTANCE.getLogger().error("https://github.com/MeGysssTaa/keiko-plugin-inspector/wiki/Installation-Instructions");
            System.exit(1);

            return;
        }

        File proxiedExecutable = new File(String.join(" ", args));

        if (!proxiedExecutable.exists()) {
            INSTANCE.getLogger().error("The specified JAR file does not exist.");
            System.exit(1);

            return;
        }

        if (!proxiedExecutable.exists()) {
            INSTANCE.getLogger().error("The specified path is a directory, not a file.");
            System.exit(1);

            return;
        }

        if (!proxiedExecutable.canRead()) {
            INSTANCE.getLogger().error("The specified JAR file cannot be read (no permissions?).");
            System.exit(1);

            return;
        }

        INSTANCE.launch(proxiedExecutable);
    }

    private void checkEnvironment() {
        SecurityManager securityManager = System.getSecurityManager();

        if (securityManager != null)
            throw new IllegalStateException(
                    "Keiko must be ran without a pre-set SecurityManager " +
                    "(detected: " + securityManager.getClass().getName() + ")");

        ClassLoader sysLoader = Objects.requireNonNull(
                ClassLoader.getSystemClassLoader(), "expected non-null sysLoader");

        ClassLoader clsLoader = Objects.requireNonNull(
                getClass().getClassLoader(), "expected non-null clsLoader");

        if (clsLoader != sysLoader)
            throw new IllegalStateException(
                    "Keiko must be launched with the default system class loader: " +
                    "expected " + sysLoader.getClass().getName() + ", " +
                    "got " + clsLoader.getClass().getName() + " (clsLoader)");

        ClassLoader thrLoader = Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader(), "expected non-null thrLoader");

        if (thrLoader != sysLoader)
            throw new IllegalStateException(
                    "Keiko must be launched with the default system class loader: " +
                    "expected " + sysLoader.getClass().getName() + ", " +
                    "got " + thrLoader.getClass().getName() + " (thrLoader)");
    }

    private void launch(File proxiedExecutable) throws Exception {
        if (started)
            throw new IllegalStateException("cannot start twice");

        started = true;
        logger.info("Launching Keiko proxy, please wait...");

        KeikoClassLoader loader = new KeikoClassLoader(proxiedExecutable);
        Thread.currentThread().setContextClassLoader(loader);
        logger.info("Loaded %d classes (%d non-fatal errors)",
                loader.getLoadResult().successes, loader.getLoadResult().failures);

        logger.info("Proxying %s", loader.getBootstrapClassName());
        Class<?> bootstrapClass = loader.findClass(loader.getBootstrapClassName());
        Method bootstrapMethod = bootstrapClass.getMethod("main", String[].class);
        bootstrapMethod.invoke(null, (Object) new String[0]);
    }

}
