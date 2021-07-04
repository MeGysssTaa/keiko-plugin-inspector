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

package me.darksidecode.keiko.proxy.injector.injection.minecraft;

import lombok.experimental.UtilityClass;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.proxy.injector.Inject;
import me.darksidecode.keiko.proxy.injector.MethodParam;
import me.darksidecode.keiko.reflect.mojang.WrappedGameProfile;
import me.darksidecode.keiko.runtimeprotect.RuntimeProtect;
import me.darksidecode.keiko.runtimeprotect.megane.event.minecraft.MinecraftOpUpdateEvent;

@UtilityClass
public class DedicatedPlayerListInjection {

    @Inject (
            inClass = "net.minecraft.server.{nms_version}.DedicatedPlayerList",
            inMethod = "addOp(Lcom/mojang/authlib/GameProfile;)V",
            at = Inject.Position.BEGINNING
    )
    public static void checkOpAdd(MethodParam<?> gameProfile) {
        // param 'gameProfile' type 'com.mojang.authlib.GameProfile'
        WrappedGameProfile wGameProfile = new WrappedGameProfile(gameProfile.getValue());
        RuntimeProtect runtimeProtect = Keiko.INSTANCE.getRuntimeProtect();

        if (runtimeProtect.isDacEnabled())
            runtimeProtect.getDac().checkOpAdd(wGameProfile.getName());

        if (runtimeProtect.isMeganeEnabled())
            runtimeProtect.getMegane().getEventBus()
                    .dispatchEvent(new MinecraftOpUpdateEvent(
                            MinecraftOpUpdateEvent.Operation.OP_ADD, wGameProfile));
    }

    @Inject (
            inClass = "net.minecraft.server.{nms_version}.DedicatedPlayerList",
            inMethod = "removeOp(Lcom/mojang/authlib/GameProfile;)V",
            at = Inject.Position.BEGINNING
    )
    public static void checkOpRemove(MethodParam<?> gameProfile) {
        // param 'gameProfile' type 'com.mojang.authlib.GameProfile'
        WrappedGameProfile wGameProfile = new WrappedGameProfile(gameProfile.getValue());
        RuntimeProtect runtimeProtect = Keiko.INSTANCE.getRuntimeProtect();

        if (runtimeProtect.isDacEnabled())
            runtimeProtect.getDac().checkOpRemove(wGameProfile.getName());

        if (runtimeProtect.isMeganeEnabled())
            runtimeProtect.getMegane().getEventBus()
                    .dispatchEvent(new MinecraftOpUpdateEvent(
                            MinecraftOpUpdateEvent.Operation.OP_REMOVE, wGameProfile));
    }

}
