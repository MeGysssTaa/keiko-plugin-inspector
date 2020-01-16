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

package me.darksidecode.keiko;

import lombok.Getter;
import me.darksidecode.keiko.config.ConfigurationLoader;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.config.InspectionsConfig;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.installer.KeikoInstaller;
import me.darksidecode.keiko.registry.PluginContext;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import me.darksidecode.keiko.staticanalysis.StaticAnalysisManager;
import me.darksidecode.keiko.util.RuntimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KeikoPluginInspector extends JavaPlugin {

    /**
     * Used to synchronize output in all streams between each other.
     */
    public static final Object outputLock = new Object();

    private static final DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
    private static final DateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss.SSS]");

    @Getter
    private static File keikoJar;

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

    static {
        // (Pre-)load before any other plugins.
        info("Booting early...");
        earlyBoot();
    }

    @Override
    public void onDisable() {
        warn("Keiko is shutting down! This may leave your server unsecured. " +
                "To prevent abuse, Keiko will shut the server down as well.");
        Bukkit.shutdown();
    }

    private static void earlyBoot() {
        keikoJar = RuntimeUtils.getSourceJar(KeikoPluginInspector.class);
        pluginsFolder = keikoJar.getParentFile();
        serverFolder = pluginsFolder.getParentFile();

        if ((!(pluginsFolder.isDirectory()))
                || (!(pluginsFolder.getName().equals("plugins"))))
            throw new RuntimeException(
                    "[FATAL] parent dir is not server plugins dir, is Keiko installed correctly?");

        workDir = new File(pluginsFolder, "Keiko/");
        installer = new KeikoInstaller(keikoJar, pluginsFolder, workDir);

        fetchKeikoVersion();

        //noinspection ResultOfMethodCallIgnored
        workDir.mkdirs();

        loadConfigurations();

        pluginContext = PluginContext.getCurrentContext(pluginsFolder);

        startRuntimeProtect();
        runStaticAnalysis(pluginsFolder);
    }

    private static void fetchKeikoVersion() {
        // Can't rely on Bukkit's Plugin#getDescription because we're booting too early.
        try (InputStream pluginYmlStream = installer.internalResource("plugin.yml");
             Reader reader = new InputStreamReader(pluginYmlStream)) {
            YamlConfiguration pluginYml = new YamlConfiguration();
            pluginYml.load(reader);

            version = pluginYml.getString("version");
        } catch (Exception ex) {
            throw new RuntimeException("failed to fetch Keiko version", ex);
        }
    }

    private static void loadConfigurations() {
        ConfigurationLoader.load(GlobalConfig.class);
        ConfigurationLoader.load(InspectionsConfig.class);
        ConfigurationLoader.load(RuntimeProtectConfig.class);
    }

    private static void runStaticAnalysis(File pluginsFolder) {
        info("Running static analysis in folder %s. " +
                "This may take some time...", pluginsFolder.getAbsolutePath());

        StaticAnalysisManager manager = new StaticAnalysisManager();
        File[] files = pluginsFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if ((file.isFile()) && (file.getName().endsWith(".jar")) && (!(file.equals(keikoJar)))) {
                    try {
                        manager.analyzeJar(file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private static void startRuntimeProtect() {
        runtimeProtect = new RuntimeProtect();

        if (RuntimeProtectConfig.getDomainAccessControlEnabled())
            runtimeProtect.setupDomainAccessControl();
    }

    public static void debug(String s, Object... format) {
        if (GlobalConfig.getEnableDebug())
            log(System.out, "DEBUG   :  " + s, format);
    }

    public static void info(String s, Object... format) {
        log(System.out, "INFO    :  " + s, format);
    }

    public static void warn(String s, Object... format) {
        log(System.err, "WARNING :  " + s, format);
    }

    private static void log(PrintStream printStream, String s, Object... format) {
        synchronized (outputLock) {
            if ((format != null) && (format.length > 0))
                s = String.format(s, format);
            printStream.println("[Keiko] " + s);

            if (GlobalConfig.getMakeLogs()) {
                try {
                    Date date = new Date();
                    String currentDate = dateFormat.format(date);

                    if (lastLogDate == null) {
                        // This is the first entry to log.
                        logWriter = new FileWriter(getLogFile(currentDate), true);
                        lastLogDate = currentDate;
                    } else if (!(currentDate.equals(lastLogDate))) {
                        // Day changed, and there was no server restart.
                        logWriter = new FileWriter(getLogFile(currentDate), true);
                        lastLogDate = currentDate;
                    }

                    s = timeFormat.format(date) + " " + s;

                    logWriter.append(s).append('\n');
                    logWriter.flush();
                } catch (IOException ex) {
                    System.err.println("Failed to log in file. Stacktrace:");
                    ex.printStackTrace();
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
