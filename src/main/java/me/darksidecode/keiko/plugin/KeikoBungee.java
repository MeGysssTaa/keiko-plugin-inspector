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

package me.darksidecode.keiko.plugin;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

@SuppressWarnings ("all")
public class KeikoBungee extends Plugin {

    @Override
    public void onEnable() {
        getProxy().getScheduler().schedule(this, () -> {
            getLogger().warning(" ");
            getLogger().warning("-----------------------------------------------------------------");
            getLogger().warning(" KEIKO IS NOT A PLUGIN!");
            getLogger().warning(" ");
            getLogger().warning(" Do not put Keiko in your 'plugins' folder!");
            getLogger().warning(" Please see the installation instructions at:");
            getLogger().warning(" >> https://github.com/MeGysssTaa/keiko-plugin-inspector/wiki/Installation-Instructions");
            getLogger().warning("-----------------------------------------------------------------");
            getLogger().warning(" ");
        }, 1, 5, TimeUnit.SECONDS);
    }

}
