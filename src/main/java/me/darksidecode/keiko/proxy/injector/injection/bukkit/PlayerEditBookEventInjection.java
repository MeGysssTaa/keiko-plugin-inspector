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
import me.darksidecode.keiko.reflect.bukkit.WrappedBookMeta;
import me.darksidecode.keiko.reflect.bukkit.WrappedPlayer;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerEditBookEvent;

@UtilityClass
public class PlayerEditBookEventInjection {

    @Inject (
            inClass = "org.bukkit.event.player.PlayerEditBookEvent",
            inMethod = "<init>(" +
                    "Lorg/bukkit/entity/Player;" +
                    "I" +
                    "Lorg/bukkit/inventory/meta/BookMeta;" +
                    "Lorg/bukkit/inventory/meta/BookMeta;" +
                    "Z" +
                    ")V",
            at = Inject.Position.BEGINNING
    )
    public static void onEditBook(MethodParam<?> player, MethodParam<Integer> slot,
                                  MethodParam<?> oldBookMeta, MethodParam<?> newBookMeta,
                                  MethodParam<Boolean> signing) {
        // param 'player' type 'org.bukkit.entity.Player'
        // param 'oldBookMeta' type 'org.bukkit.inventory.meta.BookMeta'
        // param 'newBookMeta' type 'org.bukkit.inventory.meta.BookMeta'
        RuntimeProtect runtimeProtect = Keiko.INSTANCE.getRuntimeProtect();

        if (runtimeProtect.isMeganeEnabled())
            runtimeProtect.getMegane().getEventBus()
                    .dispatchEvent(new BukkitPlayerEditBookEvent(
                            new WrappedPlayer(player.getValue()),
                            new WrappedBookMeta(oldBookMeta.getValue()),
                            new WrappedBookMeta(newBookMeta.getValue()),
                            signing.getValue()));
    }

}
