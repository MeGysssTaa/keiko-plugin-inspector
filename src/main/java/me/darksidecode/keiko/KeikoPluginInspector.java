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

package me.darksidecode.keiko;

import lombok.Getter;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.installer.KeikoInstaller;
import me.darksidecode.keiko.registry.PluginContext;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class KeikoPluginInspector {

    /**
     * Used to synchronize output in all streams between each other.
     */
    public static final Object outputLock = new Object();

    public static final String LINE = "======================================================================";

    private static final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    private static final DateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss.SSS]");

    private static boolean earlyBooted;

    @Getter
    private static Platform platform;

    @Getter
    private static File keikoJar;

    @Getter
    private static String jrePath;

    @Getter
    private static File serverFolder, pluginsFolder, workDir;

    @Getter
    private static RuntimeProtect runtimeProtect;

    @Getter
    private static PluginContext pluginContext;

    @Getter
    private static FileWriter logWriter;
    private static String lastLogDate;

    @Getter
    private static KeikoInstaller installer;

    @Getter
    private static String version;

    public static void earlyBoot(Platform currentPlatform) {
//        if (earlyBooted)
//            throw new IllegalStateException("Keiko cannot early-boot twice");
//
//        earlyBooted = true;
//        platform = currentPlatform;
//        jrePath = System.getProperty("java.home").replace("\\", "/"); // better Windows compatibility
//        keikoJar = RuntimeUtils.getSourceJar(KeikoPluginInspector.class);
//        pluginsFolder = keikoJar.getParentFile();
//        serverFolder = pluginsFolder.getParentFile();
//
//        if ((!(pluginsFolder.isDirectory()))
//                || (!(pluginsFolder.getName().equals("plugins"))))
//            throw new RuntimeException(
//                    "[FATAL] parent dir is not server plugins dir, is Keiko installed correctly?");
//
//        workDir = new File(pluginsFolder, "Keiko/");
//        installer = new KeikoInstaller(keikoJar, pluginsFolder, workDir);
//
//        //noinspection ResultOfMethodCallIgnored
//        workDir.mkdirs();
//        fetchKeikoVersion();
//
//        info("Performing early boot... [%s]", platform.name().toLowerCase());
//
//        switch (platform) {
//            case BUKKIT:
//                warn("");
//                warn(LINE);
//                warn("  (!) IMPORTANT: You are running Keiko on Bukkit.");
//                warn("      This means that Keiko will only inspect plugins installed on");
//                warn("      this certain Bukkit server, and if you are using BungeeCord,");
//                warn("      Bungee plugins will NOT be checked! You should install Keiko");
//                warn("      on the Bungee itself too if you are using it (make sure to have");
//                warn("      Keiko installed both on Bungee and ALL its 'child' servers).");
//                warn(LINE);
//                warn("");
//
//                break;
//
//            case BUNGEECORD:
//                warn("");
//                warn(LINE);
//                warn("  (!) IMPORTANT: You are running Keiko on BungeeCord.");
//                warn("      This means that Keiko will only inspect plugins installed");
//                warn("      on your Bungee, and plugins installed on your Bukkit servers");
//                warn("      will NOT be checked! You should install Keiko not only on");
//                warn("      Bungee, but on all your Bukkit ('child') servers as well!");
//                warn(LINE);
//                warn("");
//
//                break;
//
//            case STANDALONE:
//                warn("");
//                warn(LINE);
//                warn("  (!) IMPORTANT: You are running Keiko as a standalone application.");
//                warn("      This means that Keiko will not do anything on its own behalf.");
//                warn("      Instead, it will simply execute the command that you have typed");
//                warn("      in keiko-tools. This mode might be useful to run static analyses");
//                warn("      on plugins before starting your server.");
//                warn(LINE);
//                warn("");
//
//                break;
//        }
//
//        loadConfigurations();
//        deleteOldLogs();
//
//        if (platform != Platform.STANDALONE) {
//            pluginContext = PluginContext.getCurrentContext(pluginsFolder);
//
//            startRuntimeProtect();
//            runStaticCheck(pluginsFolder);
//        }
    }

    private static void fetchKeikoVersion() {
        // Can't rely on Bukkit's Plugin#getDescription because we're booting too early.
        String pluginDataFile = (platform == Platform.BUKKIT) ? "plugin.yml" : "bungee.yml";

        try (InputStream pluginYmlStream = installer.internalResource(pluginDataFile);
             Reader reader = new InputStreamReader(pluginYmlStream)) {
            YamlConfiguration pluginYml = new YamlConfiguration();
            pluginYml.load(reader);

            version = pluginYml.getString("version");
        } catch (Exception ex) {
            throw new RuntimeException("failed to fetch Keiko version", ex);
        }
    }

    private static void loadConfigurations() {
//        ConfigurationLoader.load(GlobalConfig.class);
//        ConfigurationLoader.load(InspectionsConfig.class);
//        ConfigurationLoader.load(RuntimeProtectConfig.class);
    }

    /**
     * (1) Check integrity of all installed plugins listed in inspections.yml.
     * (2) Run static inspections on all installed plugins.
     */
    private static void runStaticCheck(File pluginsFolder) {
//        info("Running static analysis in folder %s. " +
//                "This may take some time...", pluginsFolder.getAbsolutePath());
//
//        PluginsIntegrityChecker checker = new PluginsIntegrityChecker();
//        StaticAnalysisManager manager = new StaticAnalysisManager();
//
//        File[] files = pluginsFolder.listFiles();
//        boolean abortServerStartup = false;
//
//        if (files != null) {
//            for (File file : files) {
//                if ((file.isFile()) && (file.getName().endsWith(".jar")) && (!(file.equals(keikoJar)))) {
//                    try {
//                        boolean integrityOk = checker.
//                                checkIntegrity(file, pluginContext.getJarOwner(file));
//
//                        // No need to analyze corrupted plugins or plugins that
//                        // are already very likely to be infected artificially.
//                        if (integrityOk)
//                            abortServerStartup = abortServerStartup || manager.analyzeJar(file);
//                        else if (InspectionsConfig.getAbortServerStartupOnIntegrityViolation())
//                            abortServerStartup = true; // abort server startup on integrity violation (if configured)
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        }
//
//        if (abortServerStartup) {
//            // SEE JAVADOC TO METHOD me.darksidecode.keiko.staticanalysis.Countermeasures#execute
//            KeikoPluginInspector.warn("The server will be shut down forcefully (rage quit).");
//            RuntimeUtils.rageQuit();
//        }
    }

    private static void startRuntimeProtect() {
        runtimeProtect = new RuntimeProtect();

        if (RuntimeProtectConfig.getDomainAccessControlEnabled())
            runtimeProtect.setupDomainAccessControl();
    }

    public static void debug(String s, Object... format) {
//        if (GlobalConfig.getEnableDebug())
//            log(System.out, "DEBUG   :  " + s, format);
    }

    public static void info(String s, Object... format) {
        log(System.out, "INFO    :  " + s, format);
    }

    public static void warn(String s, Object... format) {
        log(System.err, "WARNING :  " + s, format);
    }

    private static void log(PrintStream printStream, String s, Object... format) {
//        synchronized (outputLock) {
//            if ((format != null) && (format.length > 0))
//                s = String.format(s, format);
//
//            printStream.println("[Keiko] " + s);
//
//            if (GlobalConfig.getMakeLogs()) {
//                try {
//                    Date date = new Date();
//                    String currentDate = dateFormat.format(date);
//
//                    if (lastLogDate == null) {
//                        // This is the first entry to log.
//                        logWriter = new FileWriter(getLogFile(currentDate), true);
//                        lastLogDate = currentDate;
//
//                        deleteOldLogs();
//                    } else if (!(currentDate.equals(lastLogDate))) {
//                        // Day changed, and there was no server restart.
//                        logWriter = new FileWriter(getLogFile(currentDate), true);
//                        lastLogDate = currentDate;
//                    }
//
//                    s = timeFormat.format(date) + " " + s;
//
//                    logWriter.append(s).append('\n');
//                    logWriter.flush();
//                } catch (IOException ex) {
//                    System.err.println("Failed to log in file. Stacktrace:");
//                    ex.printStackTrace();
//                }
//            }
//        }
    }

    private static void deleteOldLogs() {
        File logsFolder = new File(workDir, "logs/");

        //noinspection ResultOfMethodCallIgnored
        logsFolder.mkdirs();

        File[] logs = logsFolder.listFiles();

        if (logs != null) {
            long logsLifespanDays = GlobalConfig.getLogsLifespanDays();

            if (logsLifespanDays != -1) {
                try {
                    long currentTime = System.currentTimeMillis();

                    for (File log : logs) {
                        // We could also just transform the file name (YYYY-MM-dd), but in that case
                        // we wouldn't respect the TIME this log file was created or last modified.
                        // File attributes allow us to keep "yesterday's" logs when date changes.
                        BasicFileAttributes attr = Files.readAttributes(log.toPath(), BasicFileAttributes.class);
                        long lastModifiedMillis = attr.lastModifiedTime().toMillis();

                        if ((lastModifiedMillis == Long.MIN_VALUE) || (lastModifiedMillis == Long.MAX_VALUE))
                            warn("Failed to retrieve last modified date/time of log file %s", log.getName());
                        else {
                            long millisSinceLastModified = currentTime - lastModifiedMillis;
                            long daysSinceLastModified = TimeUnit.MILLISECONDS.toDays(millisSinceLastModified);

                            if (daysSinceLastModified > logsLifespanDays) {
                                //noinspection ResultOfMethodCallIgnored
                                log.delete();
                                info("Automatically deleted old log file %s", log.getName());
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException("failed to delete old logs", ex);
                }
            }
        }
    }

    private static File getLogFile(String date) {
        File logsFolder = new File(workDir, "logs/");

        //noinspection ResultOfMethodCallIgnored
        logsFolder.mkdirs();

        return new File(logsFolder, date + ".log");
    }

}
