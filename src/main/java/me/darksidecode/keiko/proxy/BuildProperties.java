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

package me.darksidecode.keiko.proxy;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;
import java.util.Properties;

public class BuildProperties {

    @Getter
    private final String version, timestamp;

    BuildProperties(@NonNull Properties properties) {
        version = Objects.requireNonNull(properties.getProperty("version"),
                "invalid build.properties: missing \"version\"");

        timestamp = Objects.requireNonNull(properties.getProperty("timestamp"),
                "invalid build.properties: missing \"timestamp\"");
    }

}
