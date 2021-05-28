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

package me.darksidecode.keiko.util;

import me.darksidecode.keiko.KeikoPluginInspector;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ConfigUtils {

    private ConfigUtils() {}

    @SuppressWarnings ("ResultOfMethodCallIgnored")
    public static YamlConfiguration loadConfig(String configName) {
        YamlConfiguration config = new YamlConfiguration();
        File configsFolder = new File(KeikoPluginInspector.getWorkDir(), "config/");

        configsFolder.mkdirs();

        File configFile = new File(configsFolder, configName + ".yml");

        if (configFile.exists()) {
            try {
                config.load(configFile);
                return config;
            } catch (IOException | InvalidConfigurationException ex) {
                File moveTo = new File(configFile.getAbsolutePath() + ".invalid.backup~");

                try {
                    Files.move(configFile.toPath(), moveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException moveEx) {
                    KeikoPluginInspector.warn("Failed to move invalid configuration file %s to %s:",
                            configFile.getAbsolutePath(), moveTo.getAbsolutePath());
                    moveEx.printStackTrace();
                    KeikoPluginInspector.warn("Please backup and/or it manually.");
                }

                KeikoPluginInspector.warn("Invalid %s configuration.", configName);
                KeikoPluginInspector.warn("The error that occurred was:");

                ex.printStackTrace();
                RuntimeUtils.rageQuit();

                throw new RuntimeException("invalid " + configName + " configuration", ex);
            }
        } else {
            // Install and retry.
            KeikoPluginInspector.getInstaller().
                    checkInstallation("config/" + configName + ".yml");

            return loadConfig(configName);
        }
    }

    public static <T> T getAuto(YamlConfiguration config, String path) {
        return (T) config.get(path);
    }

}
