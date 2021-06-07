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

        if (fqKeyPath.length == 1)
            return get(globalSection, key, def);
        else {
            Map<String, Object> lastInnerSection = resolveFinalInnerSection(fqKeyPath);
            return lastInnerSection == null ? def
                    : get(lastInnerSection, fqKeyPath[fqKeyPath.length - 1], def);
        }
    }

    private Map<String, Object> resolveFinalInnerSection(String[] fqKeyPath) {
        Map<String, Object> lastInnerSection = globalSection;

        for (int i = 0; i < fqKeyPath.length - 1; i++) {
            try {
                Map<String, Object> innerSection = get(lastInnerSection, fqKeyPath[i], null);

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

    private <T> T get(Map<String, Object> src, String key, T def) {
        return (T) src.getOrDefault(key, def);
    }

}
