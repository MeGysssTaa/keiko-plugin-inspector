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

package me.darksidecode.keiko.runtimeprotect.megane.event;

import lombok.NonNull;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerChatEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerCommandPreprocessEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerConnectionUpdateEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.craftbukkit.CraftBukkitCommandEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.minecraft.MinecraftOpUpdateEvent;

public interface Listener {

    default void onBukkitPlayerConnectionUpdate(@NonNull BukkitPlayerConnectionUpdateEvent e) {}

    default void onBukkitPlayerChat(@NonNull BukkitPlayerChatEvent e) {}

    default void onBukkitPlayerCommandPreprocess(@NonNull BukkitPlayerCommandPreprocessEvent e) {}

    default void onMinecraftOpUpdate(@NonNull MinecraftOpUpdateEvent e) {}

    default void onCraftBukkitCommand(@NonNull CraftBukkitCommandEvent e) {}

}
