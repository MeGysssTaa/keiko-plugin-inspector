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

package me.darksidecode.keiko.registry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.Platform;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Getter
@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class PluginContext {

    private final List<IndexedPlugin> plugins;

    public IndexedPlugin getJarOwner(File pluginJar) {
        return plugins.stream().filter(plugin
                -> plugin.getJar().equals(pluginJar)).findFirst().orElse(null);
    }

    public IndexedPlugin getClassOwner(String className) {
        return plugins.stream().filter(plugin
                -> plugin.getClasses().contains(className)).findFirst().orElse(null);
    }

    public IndexedPlugin getPlugin(String name) {
        return plugins.stream().filter(plugin
                -> plugin.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static PluginContext getCurrentContext(File pluginsFolder) {
        KeikoPluginInspector.info("Indexing plugins...");
        File[] files = pluginsFolder.listFiles();

        if (files != null) {
            List<IndexedPlugin> indexedPlugins = new ArrayList<>();
            String pluginDataFile =
                    (KeikoPluginInspector.getPlatform() == Platform.BUKKIT) ? "plugin.yml" : "bungee.yml";

            for (File file : files) {
                if ((file.isFile()) && (file.getName().endsWith(".jar"))) {
                    try {
                        ZipFile zipFile = new ZipFile(file);
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();

                        List<String> pluginClasses = new ArrayList<>();
                        String pluginName = null;
                        String pluginMainClass = null;

                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            String entryName = entry.getName();

                            if ((entryName.equals(pluginDataFile))) {
                                try {
                                    YamlConfiguration yaml = new YamlConfiguration();
                                    yaml.load(new InputStreamReader(zipFile.getInputStream(entry)));

                                    pluginName = yaml.getString("name");
                                    pluginMainClass = yaml.getString("main");
                                } catch (Exception ex) {
                                    KeikoPluginInspector.warn("Invalid plugin.yml in %s", file.getName());
                                }
                            } else if (entryName.endsWith(".class")) {
                                // 'my/cool/Class.class' => 'my.cool.Class'
                                String className = entryName.
                                        replace(".class", "").
                                        replace("/", ".");

                                if (className.contains("$"))
                                    // 'my.cool.Class$1' => 'my.cool.Class'
                                    className = className.split(Pattern.quote("$"))[0];

                                if (!(pluginClasses.contains(className)))
                                    pluginClasses.add(className);
                            }
                        }

                        zipFile.close();
                        String finalPluginMainClass = pluginMainClass;

                        // Validate plugin...
                        if (pluginName == null)
                            KeikoPluginInspector.warn("Invalid plugin %s: " +
                                    "missing 'name' in plugin.yml (or no plugin.yml)", file.getName());
                        else if (pluginMainClass == null)
                            KeikoPluginInspector.warn("Invalid plugin %s: " +
                                    "missing 'main' in plugin.yml (or no plugin.yml)", file.getName());
                        else if (pluginClasses.stream().noneMatch(finalPluginMainClass::equals))
                            KeikoPluginInspector.warn("Invalid plugin %s: " +
                                            "class %s is declared is main in plugin.yml but is missing in the JAR",
                                    file.getName(), pluginMainClass);
                        else {
                            // Plugin is valid, add it to context
                            indexedPlugins.add(new IndexedPlugin(file, pluginClasses, pluginName, pluginMainClass));
                            KeikoPluginInspector.debug("Indexed plugin %s with name %s. Classes: %s. Main class: %s",
                                    file.getName(), pluginName, pluginClasses.size(), pluginMainClass);
                        }
                    } catch (Exception ex) {
                        KeikoPluginInspector.warn("Failed to index plugin %s, is it valid?", file.getName());
                        KeikoPluginInspector.warn("Stacktrace:");

                        ex.printStackTrace();
                    }
                }
            }

            KeikoPluginInspector.info("%s plugins have been indexed successfully.", indexedPlugins.size());

            return new PluginContext(indexedPlugins);
        } else
            throw new RuntimeException("missing plugins folder???");
    }

}