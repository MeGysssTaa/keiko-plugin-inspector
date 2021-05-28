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
