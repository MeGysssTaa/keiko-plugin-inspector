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
import me.darksidecode.keiko.staticanalysis.FailurePolicy;

public final class GlobalConfig {

    private GlobalConfig() {}

    @Getter @Config
    private static String locale = "en_US";

    @Getter @Config
    private static Integer updatesCheckFreqMins = 120;

    @Getter @Config
    private static Boolean makeLogs = true;

    @Getter @Config
    private static Integer logsLifespanDays = 14;

    @Getter @Config
    private static Boolean enableDebug = false;

    @Getter @Config
    private static FailurePolicy failurePolicy = FailurePolicy.SHUTDOWN;

    @Getter @Config
    private static Boolean allowKeikoRageQuit = true;

}
