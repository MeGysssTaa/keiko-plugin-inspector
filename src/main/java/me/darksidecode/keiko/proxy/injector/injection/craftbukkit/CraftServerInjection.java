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

package me.darksidecode.keiko.proxy.injector.injection.craftbukkit;

import lombok.experimental.UtilityClass;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.proxy.injector.Inject;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import me.darksidecode.keiko.runtimeprotect.megane.event.craftbukkit.CraftBukkitCommandEvent;

@UtilityClass
public class CraftServerInjection {

    @Inject (
            inClass = "org.bukkit.craftbukkit.{nms_version}.CraftServer",
            inMethod = "dispatchCommand(" +
                    "Lorg/bukkit/command/CommandSender;" +
                    "Ljava/lang/String;" +
                    ")Z",
            at = Inject.Position.BEGINNING
    )
    public static void checkCommandDispatch() {
        onCommand();
    }

    @Inject (
            inClass = "org.bukkit.craftbukkit.{nms_version}.CraftServer",
            inMethod = "dispatchServerCommand(" +
                    "Lorg/bukkit/command/CommandSender;" +
                    "Lnet/minecraft/server/v1_8_R3/ServerCommand;" +
                    ")Z",
            at = Inject.Position.BEGINNING
    )
    public static void checkCommandDispatchServer() {
        onCommand();
    }

    private static void onCommand() {
        RuntimeProtect runtimeProtect = Keiko.INSTANCE.getRuntimeProtect();
        if (runtimeProtect.isDacEnabled()) runtimeProtect.getDac().checkCommandDispatch();

        if (runtimeProtect.isMeganeEnabled())
            runtimeProtect.getMegane().getEventBus()
                    .dispatchEvent(new CraftBukkitCommandEvent());
    }

}
