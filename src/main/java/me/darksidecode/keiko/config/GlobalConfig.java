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

import lombok.Getter;
import me.darksidecode.keiko.io.KeikoLogger;

public final class GlobalConfig {

    private GlobalConfig() {}

    @Getter
    private static YamlHandle handle;

    @Getter @Config
    private static String locale = "system";

    @Getter @Config ("updater.interval_minutes")
    private static Integer updaterIntervalMinutes = 120;

    @Getter @Config ("updater.download")
    private static Boolean updaterDownload = true;

    @Getter @Config
    private static Integer logsLifespanDays = 14;

    @Getter @Config ("log_level.console")
    private static KeikoLogger.Level logLevelConsole = KeikoLogger.Level.INFO;

    @Getter @Config ("log_level.file")
    private static KeikoLogger.Level logLevelFile = KeikoLogger.Level.DEBUG;

    @Getter @Config
    private static Boolean abortOnError = true;

}
