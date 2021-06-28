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

package me.darksidecode.keiko.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Map;

@RequiredArgsConstructor
public class YamlHandle {

    @NonNull
    private final Map<String, Object> globalSection;

    public <T> T get(String key) {
        return get(key, null);
    }

    public <T> T require(String key) {
        T result = get(key);

        if (result == null)
            throw new YAMLException("missing configuration key: " + key);

        return result;
    }

    public <T> T get(@NonNull String key, T def) {
        String[] fqKeyPath = key.split("\\.");
        Map<String, Object> lastInnerSection = resolveFinalInnerSection(fqKeyPath);
        return lastInnerSection == null ? def
                : (T) lastInnerSection.getOrDefault(fqKeyPath[fqKeyPath.length - 1], def);
    }

    private Map<String, Object> resolveFinalInnerSection(String[] fqKeyPath) {
        Map<String, Object> lastInnerSection = globalSection;

        for (int i = 0; i < fqKeyPath.length - 1; i++) {
            try {
                Map<String, Object> innerSection
                        = (Map<String, Object>) lastInnerSection.get(fqKeyPath[i]);

                if (innerSection != null)
                    lastInnerSection = innerSection;
                else
                    return null;
            } catch (ClassCastException ex) {
                return null;
            }
        }

        return lastInnerSection;
    }

}
