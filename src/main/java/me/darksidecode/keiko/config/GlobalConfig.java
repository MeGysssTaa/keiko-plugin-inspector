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

import lombok.Getter;
import me.darksidecode.keiko.io.KeikoLogger;

public final class GlobalConfig {

    private GlobalConfig() {}

    @Getter
    private static YamlHandle handle;

    @Getter @Config
    private static String locale = "system";

    @Getter @Config
    private static Integer updatesCheckFreqMins = 120;

    @Getter @Config
    private static Integer logsLifespanDays = 14;

    @Getter @Config ("log_level.console")
    private static KeikoLogger.Level logLevelConsole = KeikoLogger.Level.INFO;

    @Getter @Config ("log_level.file")
    private static KeikoLogger.Level logLevelFile = KeikoLogger.Level.DEBUG;

    @Getter @Config
    private static Boolean abortOnError = true;

}
