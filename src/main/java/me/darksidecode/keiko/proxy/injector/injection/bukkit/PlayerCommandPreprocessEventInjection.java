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
import me.darksidecode.keiko.reflect.WrappedBukkitPlayer;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerCommandPreprocessEvent;

import java.util.Set;

@UtilityClass
public class PlayerCommandPreprocessEventInjection {

    @Inject (
            inClass = "org.bukkit.event.player.PlayerCommandPreprocessEvent",
            inMethod = "<init>(Lorg/bukkit/entity/Player;Ljava/lang/String;)V",
            at = Inject.Position.BEGINNING
    )
    public static void onCmdPreprocess1(MethodParam<?> player, MethodParam<String> msg) {
        // param 'player' type 'org.bukkit.entity.Player'
        onCmdPreprocess(player, msg);
    }

    @Inject (
            inClass = "org.bukkit.event.player.PlayerCommandPreprocessEvent",
            inMethod = "<init>(Lorg/bukkit/entity/Player;Ljava/lang/String;Ljava/util/Set;)V",
            at = Inject.Position.BEGINNING
    )
    public static void onCmdPreprocess2(MethodParam<?> player, MethodParam<String> msg,
                                        MethodParam<Set<?>> recipients) {
        // param 'player' type 'org.bukkit.entity.Player'
        // param 'recipients' type 'java.util.Set<org.bukkit.entity.Player>'
        onCmdPreprocess(player, msg);
    }

    private static void onCmdPreprocess(MethodParam<?> player, MethodParam<String> msg) {
        RuntimeProtect runtimeProtect = Keiko.INSTANCE.getRuntimeProtect();

        if (runtimeProtect.isMeganeEnabled())
            runtimeProtect.getMegane().getEventBus()
                    .dispatchEvent(new BukkitPlayerCommandPreprocessEvent(
                            new WrappedBukkitPlayer(player.getValue()), msg.getValue()));
    }

}
