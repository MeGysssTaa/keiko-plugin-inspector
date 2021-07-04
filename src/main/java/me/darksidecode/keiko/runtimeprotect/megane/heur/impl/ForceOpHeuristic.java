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
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.reflect.bukkit.WrappedBukkit;
import me.darksidecode.keiko.reflect.bukkit.WrappedBukkitPlayer;
import me.darksidecode.keiko.registry.Identity;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerChatEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerCommandPreprocessEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.bukkit.BukkitPlayerConnectionUpdateEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.craftbukkit.CraftBukkitCommandEvent;
import me.darksidecode.keiko.runtimeprotect.megane.event.minecraft.MinecraftOpUpdateEvent;
import me.darksidecode.keiko.runtimeprotect.megane.heur.Heuristic;
import me.darksidecode.keiko.runtimeprotect.megane.heur.RegisterHeuristic;
import me.darksidecode.keiko.time.AtomicClock;
import me.darksidecode.keiko.time.Clock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RegisterHeuristic ({
        BukkitPlayerConnectionUpdateEvent.class,
        BukkitPlayerCommandPreprocessEvent.class,
        MinecraftOpUpdateEvent.class,
        CraftBukkitCommandEvent.class,
})
public class ForceOpHeuristic extends Heuristic {

    private static final long THRESHOLD_MILLIS = 3000;

    @Override
    public void onBukkitPlayerConnectionUpdate(@NonNull BukkitPlayerConnectionUpdateEvent e) {
        if (e.getOperation() == BukkitPlayerConnectionUpdateEvent.Operation.JOIN)
            PlayerStates.of(e.getPlayer().getName()).joinClock.reset();
        else if (e.getOperation() == BukkitPlayerConnectionUpdateEvent.Operation.QUIT)
            PlayerStates.map.remove(e.getPlayer().getName());
    }

    @Override
    public void onBukkitPlayerChat(@NonNull BukkitPlayerChatEvent e) {
        PlayerStates.of(e.getPlayer().getName()).chatClock.reset();
    }

    @Override
    public void onBukkitPlayerCommandPreprocess(@NonNull BukkitPlayerCommandPreprocessEvent e) {
        PlayerStates.of(e.getPlayer().getName()).cmdPrepClock.reset();
    }

    @Override
    public void onMinecraftOpUpdate(@NonNull MinecraftOpUpdateEvent e) {
        Identity plugin;

        if (!e.isIssuedByPlugin() || isExcluded(plugin = e.getPluginStates().getPlugin()))
            return;

        if (e.getOperation() == MinecraftOpUpdateEvent.Operation.OP_ADD) {
            String player = e.getGameProfile().getName();
            PlayerStates states = PlayerStates.of(player);

            if (!states.joinClock.hasElapsed(THRESHOLD_MILLIS))
                handleDetection(plugin, "onJoin", "bukkitApi", player);
            else if (!states.chatClock.hasElapsed(THRESHOLD_MILLIS))
                handleDetection(plugin, "onChat", "bukkitApi", player);
            else if (!states.cmdPrepClock.hasElapsed(THRESHOLD_MILLIS))
                handleDetection(plugin, "onCmdPrep", "bukkitApi", player);
        }
    }

    @Override
    public void onCraftBukkitCommand(@NonNull CraftBukkitCommandEvent e) {
        Identity plugin;

        if (!e.isIssuedByPlugin() || isExcluded(plugin = e.getPluginStates().getPlugin()))
            return;

        if (e.getCommand().startsWith("op ")) {
            String player = e.getCommand().split(" ")[1];
            PlayerStates states = PlayerStates.of(player);

            if (!states.joinClock.hasElapsed(THRESHOLD_MILLIS))
                handleDetection(plugin, "onJoin", "opCmd", player);
            else if (!states.chatClock.hasElapsed(THRESHOLD_MILLIS))
                handleDetection(plugin, "onChat", "opCmd", player);
            else if (!states.cmdPrepClock.hasElapsed(THRESHOLD_MILLIS))
                handleDetection(plugin, "onCmdPrep", "opCmd", player);
        }
    }

    private void handleDetection(Identity plugin, String when, String how, String player) {
        // TODO #1 extract this to some more generic code suitable for all heuristics (refactor).
        // TODO #2 more noticeable notifications, for example, using a Discord bot or e-mail.
        Keiko.INSTANCE.getLogger().warning("=====================================================================");
        Keiko.INSTANCE.getLogger().warning("\n\n\n\n\n");
        Keiko.INSTANCE.getLogger().warningLocalized(i18nPrefix + "title");
        Keiko.INSTANCE.getLogger().warningLocalized(i18nPrefix + when,
                player, plugin.getPluginName(), plugin.getClassName(), plugin.getMethodName());
        Keiko.INSTANCE.getLogger().warningLocalized(i18nPrefix + how);
        Keiko.INSTANCE.getLogger().warning("\n\n\n\n\n");
        Keiko.INSTANCE.getLogger().warning("=====================================================================");

        if (remediate) {
            try {
                remediate(player);
            } catch (Throwable t) {
                Keiko.INSTANCE.getLogger().warningLocalized("runtimeProtect.megane.remedFailure");
                Keiko.INSTANCE.getLogger().error("Unhandled error during remediation", t);
            }
        }
    }

    private void remediate(String playerToDeop) {
        // Deop the player that was just Force-OP'ped.
        Object playerHandle = WrappedBukkit.getPlayerExact(playerToDeop);
        WrappedBukkitPlayer wPlayer = new WrappedBukkitPlayer(playerHandle);
        wPlayer.setOp(false);
        Keiko.INSTANCE.getLogger().infoLocalized("runtimeProtect.megane.remedSuccess");
        Keiko.INSTANCE.getLogger().infoLocalized(i18nPrefix + "remedSuccessDetails", playerToDeop);
    }

    private static class PlayerStates {
        private static final Map<String, PlayerStates> map = new ConcurrentHashMap<>();

        private final Clock joinClock    = new AtomicClock();
        private final Clock chatClock    = new AtomicClock();
        private final Clock cmdPrepClock = new AtomicClock();

        private static PlayerStates of(String playerName) {
            return map.computeIfAbsent(playerName, k -> new PlayerStates());
        }
    }

}
