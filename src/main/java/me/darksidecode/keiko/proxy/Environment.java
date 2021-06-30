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

package me.darksidecode.keiko.proxy;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import me.darksidecode.keiko.registry.PluginContext;

import java.io.File;

@Getter (onMethod_ = @Synchronized) // makes all getters synchronize on a private $lock field (lombok-generated)
@Setter (onMethod_ = @Synchronized) // makes all setters synchronize on a private $lock field (lombok-generated)
public final class Environment {

    private BuildProperties buildProperties;

    private File keikoExecutable;

    private File serverDir;

    private File workDir;

    private File pluginsDir;

    private PluginContext pluginContext;

    private String nmsVersion;

}
