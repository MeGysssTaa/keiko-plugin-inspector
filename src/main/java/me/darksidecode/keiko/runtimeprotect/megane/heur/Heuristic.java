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

package me.darksidecode.keiko.runtimeprotect.megane.heur;

import lombok.Getter;
import lombok.NonNull;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.config.YamlHandle;
import me.darksidecode.keiko.registry.Identity;
import me.darksidecode.keiko.registry.IdentityFilter;
import me.darksidecode.keiko.runtimeprotect.megane.event.Listener;
import me.darksidecode.keiko.util.ConfigurationUtils;

import java.util.List;

public class Heuristic implements Listener {

    @Getter
    private final boolean enabled;

    protected final boolean remediate;

    private final List<IdentityFilter> exclusions;

    @Getter
    protected final String displayName, configSection, i18nPrefix;

    public Heuristic() {
        // Infer displayName and configSection from class name.
        String name = getClass().getSimpleName().replace("Heuristic", "");
        displayName = "Heur." + name;

        char[] nameChars = name.toCharArray();
        StringBuilder configNameBuilder = new StringBuilder();
        boolean firstChar = true;

        for (char c : nameChars) {
            if (firstChar)
                firstChar = false;
            else if (Character.isUpperCase(c))
                configNameBuilder.append('_');

            configNameBuilder.append(Character.toLowerCase(c));
        }

        String snakeCaseName = configNameBuilder.toString();

        configSection = "megane.heur." + snakeCaseName;
        i18nPrefix = "runtimeProtect.megane.heur." + snakeCaseName + ".";

        // Load configuration.
        YamlHandle conf = RuntimeProtectConfig.getHandle();

        enabled = conf.get(configSection + ".enabled", false);
        remediate = conf.get(configSection + ".remediate", false);
        exclusions = ConfigurationUtils.getExclusionsList(conf, configSection + ".exclusions");
    }

    protected final boolean isExcluded(@NonNull Identity identity) {
        return exclusions.stream()
                .anyMatch(exclusion -> exclusion.matches(identity));
    }

    protected final <T> T getLocalSetting(@NonNull String key, T def) {
        return RuntimeProtectConfig.getHandle().get(configSection + "." + key, def);
    }

}
