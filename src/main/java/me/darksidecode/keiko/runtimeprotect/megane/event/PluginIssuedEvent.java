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

package me.darksidecode.keiko.runtimeprotect.megane.event;

import lombok.Getter;
import me.darksidecode.keiko.registry.Identity;
import me.darksidecode.keiko.runtimeprotect.megane.heur.PluginStates;
import me.darksidecode.keiko.util.RuntimeUtils;

/**
 * Indicates an even that COULD have theoretically been triggered by a plugin.
 * Does NOT guarantee that the event was not triggered by the Minecraft server
 * (or Bukkit). Keep this in mind!
 */
public abstract class PluginIssuedEvent implements Event {

    @Getter
    protected final PluginStates pluginStates; // MAY be null! This class does NOT guarantee the event is plugin-issued!

    protected PluginIssuedEvent() {
        Identity plugin = RuntimeUtils.resolveCallerPlugin();
        pluginStates = plugin == null ? null : PluginStates.of(plugin);
    }

    public final boolean isIssuedByPlugin() {
        return pluginStates != null;
    }

}
