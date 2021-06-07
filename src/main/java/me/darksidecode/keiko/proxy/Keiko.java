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
import me.darksidecode.keiko.config.ConfigurationLoader;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.config.InspectionsConfig;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.installer.KeikoInstaller;

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
    private BuildProperties buildProperties;

    @Getter
    private File workDir;

    @Getter
    private final KeikoLogger logger;

    @SuppressWarnings ("UseOfSystemOutOrSystemErr")
    private Keiko() {
        checkEnvironment();
        fetchBuildProperties();
        installEverything();

        System.out.println(LOGO);
        System.out.println("      --  " + buildProperties.getVersion());
        System.out.println("      --  " + buildProperties.getTimestamp());
        System.out.println("\n\n");

        logger = new KeikoLogger(new File(workDir, "logs"));

        if (GlobalConfig.getEnableDebug())
            logger.debugLocalized("startup.debugEnabled");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            INSTANCE.getLogger().warningLocalized("startup.noArgsErr.line1");
            INSTANCE.getLogger().warningLocalized("startup.noArgsErr.line2");
            INSTANCE.getLogger().warningLocalized("startup.noArgsErr.line3");
            System.exit(1);

            return;
        }

        File proxiedExecutable = new File(String.join(" ", args));

        if (!proxiedExecutable.exists()) {
            INSTANCE.getLogger().warningLocalized("startup.jarErr.notExists");
            System.exit(1);

            return;
        }

        if (!proxiedExecutable.exists()) {
            INSTANCE.getLogger().warningLocalized("startup.jarErr.isDir");
            System.exit(1);

            return;
        }

        if (!proxiedExecutable.canRead()) {
            INSTANCE.getLogger().warningLocalized("startup.jarErr.cantRead");
            System.exit(1);

            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(
                INSTANCE::shutdown, "Keiko Proxy Shutdown Hook"));

        INSTANCE.launch(proxiedExecutable);
    }

    private void checkEnvironment() {
        SecurityManager securityManager = System.getSecurityManager();

        if (securityManager != null)
            throw new IllegalStateException(
                    "Keiko must be ran without a pre-set SecurityManager " +
                    "(detected: " + securityManager.getClass().getName() + ")");

        String propSysLoader = System.getProperty("java.system.class.loader");

        if (propSysLoader != null)
            throw new IllegalStateException(
                    "Keiko must be launched with the default system class loader " +
                    "(detected: " + propSysLoader + ")");

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

    private void fetchBuildProperties() {
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("build.properties")) {
            Properties properties = new Properties();
            properties.load(stream);
            buildProperties = new BuildProperties(properties);
        } catch (IOException ex) {
            throw new RuntimeException("failed to load build.properties", ex);
        }
    }

    private void installEverything() {
        workDir = new File(KeikoProperties.workDir);
        //noinspection ResultOfMethodCallIgnored
        workDir.mkdirs();

        KeikoInstaller installer = new KeikoInstaller(workDir);

        ConfigurationLoader.load(installer, GlobalConfig.class);
        ConfigurationLoader.load(installer, InspectionsConfig.class);
        ConfigurationLoader.load(installer, RuntimeProtectConfig.class);
    }

    private void launch(File proxiedExecutable) throws Exception {
        if (started)
            throw new IllegalStateException("cannot start twice");

        started = true;
        logger.infoLocalized("startup.launchingProxy");

        KeikoClassLoader loader = new KeikoClassLoader(proxiedExecutable);
        Thread.currentThread().setContextClassLoader(loader);
        logger.infoLocalized("startup.classLoaderStats",
                loader.getLoadResult().successes, loader.getLoadResult().failures);

        logger.infoLocalized("startup.proxyBegin", loader.getBootstrapClassName());
        Class<?> bootstrapClass = loader.findClass(loader.getBootstrapClassName());
        Method bootstrapMethod = bootstrapClass.getMethod("main", String[].class);
        bootstrapMethod.invoke(null, (Object) new String[0]);
    }

    private void shutdown() {
        try {
            logger.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //noinspection UseOfSystemOutOrSystemErr
        System.out.println("[Keiko] Bye!");
    }

}
