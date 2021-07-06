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

package me.darksidecode.keiko.runtimeprotect.megane.heur.impl;

import lombok.NonNull;
import me.darksidecode.keiko.io.KeikoLogger;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.reflect.bukkit.WrappedBookMeta;
import me.darksidecode.keiko.reflect.bukkit.WrappedPlayer;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerEditBookEvent;
import me.darksidecode.keiko.runtimeprotect.megane.heur.Heuristic;
import me.darksidecode.keiko.runtimeprotect.megane.heur.RegisterHeuristic;

@RegisterHeuristic ({
        BukkitPlayerEditBookEvent.class
})
public class BookBackdoorHeuristic extends Heuristic {

    @Override
    public void onBukkitPlayerEditBookEvent(@NonNull BukkitPlayerEditBookEvent e) {
        if (e.isSigning() && (isSuspicious(e.getOldBookMeta()) || isSuspicious(e.getNewBookMeta())))
            handleDetection(e.getPlayer());
    }

    private void handleDetection(WrappedPlayer player) {
        // TODO #1 extract this to some more generic code suitable for all heuristics (refactor).
        // TODO #2 more noticeable notifications, for example, using a Discord bot or e-mail.
        Keiko.INSTANCE.getLogger().warning("\n\n");
        Keiko.INSTANCE.getLogger().warning(KeikoLogger.RED,
                "=====================================================================");
        Keiko.INSTANCE.getLogger().warning("\n\n\n\n\n\n");
        Keiko.INSTANCE.getLogger().warningLocalized(KeikoLogger.RED,
                "runtimeProtect.megane.threatDetected", displayName);
        Keiko.INSTANCE.getLogger().warning(" ");
        Keiko.INSTANCE.getLogger().warningLocalized(KeikoLogger.RED,
                i18nPrefix + "details", player);
        Keiko.INSTANCE.getLogger().warning("\n\n\n\n\n\n");
        Keiko.INSTANCE.getLogger().warning(KeikoLogger.RED,
                "=====================================================================");
        Keiko.INSTANCE.getLogger().warning("\n\n");
    }

    private static boolean isSuspicious(WrappedBookMeta bookMeta) {
        return bookMeta.hasTitle() && bookMeta.getTitle().equals("cmd");
    }

}
