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
                                                            PhaseExecutionException error) throws Throwable {
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
