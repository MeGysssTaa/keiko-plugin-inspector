/*
 * Copyright 2020 DarksideCode
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

package me.darksidecode.keiko;

import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.installer.UpdatesCheckerTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KeikoBukkit extends JavaPlugin {

    static {
        // (Pre-)load before any other plugins.
        KeikoPluginInspector.earlyBoot(Platform.BUKKIT);
    }

    @Override
    public void onEnable() {
        int updatesCheckFreqMins = GlobalConfig.getUpdatesCheckFreqMins();

        if (updatesCheckFreqMins >= 0) { // -1 = don't ever check for updates
            if (updatesCheckFreqMins == 0) // Periodic checking is disabled - only check for updates once, at startup.
                Bukkit.getScheduler().runTaskAsynchronously(this, new UpdatesCheckerTask());
            else {
                long updatesCheckFreqTicks = updatesCheckFreqMins * 60 * 20; // 1 second contains 20 ticks
                Bukkit.getScheduler().runTaskTimerAsynchronously(
                        this, new UpdatesCheckerTask(), 0, updatesCheckFreqTicks);
            }
        }
    }

    @Override
    public void onDisable() {
        // Used to avoid plugman/reload-related issues
        KeikoPluginInspector.warn("Keiko is shutting down! This may leave your server unsecured. " +
                "To prevent abuse, Keiko will shut the server down as well.");
        Bukkit.shutdown();
    }

}
