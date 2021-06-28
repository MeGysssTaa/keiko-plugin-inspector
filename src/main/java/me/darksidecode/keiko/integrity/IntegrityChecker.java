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

package me.darksidecode.keiko.integrity;

import lombok.NonNull;
import me.darksidecode.keiko.config.InspectionsConfig;
import me.darksidecode.keiko.i18n.I18n;
import me.darksidecode.keiko.io.UserInputRequest;
import me.darksidecode.keiko.io.YesNo;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.registry.IndexedPlugin;
import me.darksidecode.keiko.registry.PluginContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class IntegrityChecker {

    private final ChecksumsCacheFile cacheFile = new ChecksumsCacheFile();

    public boolean run(@NonNull PluginContext pluginContext) {
        // Key - plugin name. Value - plugin jar SHA-512 checksum string.
        Map<String, String> checksums = cacheFile.read();
        int readHash = checksums.hashCode();
        Collection<String> excludedPlugins = null;

        try {
            excludedPlugins = InspectionsConfig.getHandle().get("integrity_exclusions");
        } catch (Exception ignored) {}

        if (excludedPlugins == null)
            excludedPlugins = Collections.EMPTY_LIST;

        for (IndexedPlugin plugin : pluginContext.getPlugins()) {
            String plugName = plugin.getName();

            if (excludedPlugins.contains(plugName))
                continue;

            String cachedChecksum = checksums.get(plugName);
            String actualChecksum = plugin.getSha512();

            if (cachedChecksum == null)
                // Cache plugin's current checksum automatically.
                updateChecksum(checksums, plugName, actualChecksum);
            else if (!cachedChecksum.equalsIgnoreCase(actualChecksum)) {
                // Checksum has changed. Ask the user whether they want to update it or abort server startup.
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "integrityChecker.violationPlugin", plugName);
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "integrityChecker.violationCached", shorten(cachedChecksum));
                Keiko.INSTANCE.getLogger().warningLocalized(
                        "integrityChecker.violationActual", shorten(actualChecksum));

                // Message like "Update checksum? [yes/no]"
                String prompt = I18n.get("integrityChecker.updatePrompt")
                        + " [" + I18n.get("prompts.yes") + "/" + I18n.get("prompts.no") + "]";

                // Prompt user to enter "yes" or "no" explicitly.
                boolean updateChecksum = UserInputRequest.newBuilder(System.in, YesNo.class)
                        .prompt(Keiko.INSTANCE.getLogger(), prompt)
                        .lineTransformer(String::trim)
                        .build()
                        .block()
                        .toBoolean();

                if (updateChecksum)
                    // The user said this checksum change was intended. Cache the new checksum.
                    updateChecksum(checksums, plugName, actualChecksum);
                else
                    // The user said this checksum change was not intended. Infected/self-modiying plugin?
                    // Abort server startup immediately - without checking the checksums of other plugins.
                    return true; // yes, abort startup
            }
        }

        // Save new and updated checksums (if any).
        int newHash = checksums.hashCode();

        if (newHash != readHash)
            cacheFile.save(checksums);

        return false; // no, do not abort startup
    }

    private void updateChecksum(Map<String, String> checksums, String plugName, String checksum) {
        checksums.put(plugName, checksum);
        Keiko.INSTANCE.getLogger().debugLocalized("integrityChecker.updated", plugName, checksum);
    }

    private static String shorten(String checksum) {
        // Only display the first 7 and the last 7 characters of the checksum,
        // because the entire string is huge - 128 characters long for SHA-512.
        return checksum.substring(0, 7) + "..." + checksum.substring(checksum.length() - 7);
    }

}
