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

package me.darksidecode.keiko.runtimeprotect.megane.event.bukkit;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.reflect.bukkit.WrappedBookMeta;
import me.darksidecode.keiko.reflect.bukkit.WrappedPlayer;
import me.darksidecode.keiko.runtimeprotect.megane.event.Event;
import me.darksidecode.keiko.runtimeprotect.megane.event.Listener;

@RequiredArgsConstructor
public class BukkitPlayerEditBookEvent implements Event {

    @Getter @NonNull
    private final WrappedPlayer player;

    @Getter @NonNull
    private final WrappedBookMeta oldBookMeta, newBookMeta;

    @Getter
    private final boolean signing;

    @Override
    public void dispatch(@NonNull Listener listener) {
        listener.onBukkitPlayerEditBookEvent(this);
    }

}
