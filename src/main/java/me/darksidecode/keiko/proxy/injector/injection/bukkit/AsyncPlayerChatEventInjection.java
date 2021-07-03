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

package me.darksidecode.keiko.proxy.injector.injection.bukkit;

import lombok.experimental.UtilityClass;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.proxy.injector.Inject;
import me.darksidecode.keiko.proxy.injector.MethodParam;
import me.darksidecode.keiko.reflect.bukkit.WrappedBukkitPlayer;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerChatEvent;

import java.util.Set;

@UtilityClass
public class AsyncPlayerChatEventInjection {

    @Inject (
            inClass = "org.bukkit.event.player.AsyncPlayerChatEvent",
            inMethod = "<init>(ZLorg/bukkit/entity/Player;Ljava/lang/String;Ljava/util/Set;)V",
            at = Inject.Position.BEGINNING
    )
    public static void onChat(MethodParam<Boolean> async, MethodParam<?> player,
                              MethodParam<String> message, MethodParam<Set<?>> recipients) {
        // param 'player' type 'org.bukkit.entity.Player'
        // param 'recipients' type 'java.util.Set<org.bukkit.entity.Player>'
        RuntimeProtect runtimeProtect = Keiko.INSTANCE.getRuntimeProtect();

        if (runtimeProtect.isMeganeEnabled())
            runtimeProtect.getMegane().getEventBus()
                    .dispatchEvent(new BukkitPlayerChatEvent(
                            new WrappedBukkitPlayer(player.getValue()), message.getValue()));
    }

}
