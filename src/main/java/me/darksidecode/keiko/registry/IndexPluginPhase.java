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

package me.darksidecode.keiko.registry;

import me.darksidecode.jminima.phase.EmittedValue;
import me.darksidecode.jminima.phase.Phase;
import me.darksidecode.jminima.phase.PhaseExecutionException;
import me.darksidecode.jminima.util.JarFileData;
import me.darksidecode.keiko.config.YamlHandle;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class IndexPluginPhase extends Phase<JarFile, IndexedPlugin> {

    @Override
    public Class<? super JarFile> getTargetTypeClass() {
        return JarFile.class;
    }

    @Override
    protected EmittedValue<? extends IndexedPlugin> execute(JarFile target,
                                                            PhaseExecutionException error) throws Exception {
        if (target == null)
            return new EmittedValue<>(new PhaseExecutionException(
                    true, "failed to index a plugin", error));

        return indexPlugin(target);
    }

    private EmittedValue<? extends IndexedPlugin> indexPlugin(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();
        Collection<String> pluginClasses = new HashSet<>();
        String pluginName = null;

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.equals("plugin.yml") || entryName.equals("bungee.yml")) {
                try (Reader reader = new BufferedReader(new InputStreamReader(
                        jarFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                    Yaml yaml = new Yaml();
                    YamlHandle handle = new YamlHandle(yaml.load(reader));

                    if (pluginName == null)
                        pluginName = handle.get("name");
                } catch (Exception ex) {
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            "pluginsIndex.invalidPluginYml", jarFile.getName());

                    return new EmittedValue<>(new PhaseExecutionException(
                            true, "invalid plugin.yml in " + jarFile.getName(), ex));
                }
            } else if (JarFileData.isClassEntry(entry)) {
                // 'my/cool/Class.class[/]' --> 'my.cool.Class'
                String className = entryName
                        .replace(".class/", "")
                        .replace(".class", "")
                        .replace("/", ".");

                pluginClasses.add(className);
            }
        }

        // Validate plugin...
        if (pluginName == null) {
            Keiko.INSTANCE.getLogger().warningLocalized(
                    "pluginsIndex.invalidPluginYml", jarFile.getName());

            return new EmittedValue<>(new PhaseExecutionException(
                    true, "invalid plugin.yml in " + jarFile.getName()));
        } else {
            // Plugin is valid, add it to context
            Keiko.INSTANCE.getLogger().debugLocalized(
                    "pluginsIndex.indexedInfo",
                    pluginName, jarFile.getName(), pluginClasses.size());

            File pluginFile = new File(jarFile.getName());
            String sha512 = StringUtils.sha512(pluginFile);

            return new EmittedValue<>(
                   new IndexedPlugin(pluginFile, pluginClasses, pluginName, sha512));
        }
    }

}
