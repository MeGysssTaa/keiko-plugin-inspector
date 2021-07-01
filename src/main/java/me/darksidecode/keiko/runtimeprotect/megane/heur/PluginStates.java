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

package me.darksidecode.keiko.runtimeprotect.megane.heur;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.registry.Identity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public final class PluginStates {

    @Getter (AccessLevel.PRIVATE) // hide getter
    private static final Map<Identity, PluginStates> map = new ConcurrentHashMap<>();

    public static PluginStates of(@NonNull Identity plugin) {
        return map.computeIfAbsent(plugin, PluginStates::new);
    }

    private final Identity plugin;

    private final Object lock = new Object();

}
