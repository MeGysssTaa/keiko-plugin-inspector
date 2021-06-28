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

package me.darksidecode.keiko;

import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.installer.UpdatesCheckerTask;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class KeikoBungee extends Plugin {

    static {
        // (Pre-)load before any other plugins.
        KeikoPluginInspector.earlyBoot(Platform.BUNGEECORD);
    }

    @Override
    public void onEnable() {
        int updatesCheckFreqMins = GlobalConfig.getUpdatesCheckFreqMins();

        if (updatesCheckFreqMins >= 0) { // -1 = don't ever check for updates
            if (updatesCheckFreqMins == 0) // Periodic checking is disabled - only check for updates once, at startup.
                getProxy().getScheduler().runAsync(this, new UpdatesCheckerTask());
            else
                getProxy().getScheduler().schedule(this,
                        new UpdatesCheckerTask(), 0, updatesCheckFreqMins, TimeUnit.MINUTES);
        }
    }

}
