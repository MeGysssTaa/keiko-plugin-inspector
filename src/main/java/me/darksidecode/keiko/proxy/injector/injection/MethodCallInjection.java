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

package me.darksidecode.keiko.proxy.injector.injection;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.injector.Inject;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Inserts a call to a given public static method in the beginning or in the end of the given method.
 */
public class MethodCallInjection extends Injection {

    /**
     * The method call to which is injected.
     */
    private final String targetClass, targetMethod;

    /**
     * Relative method code injection position.
     */
    private final Inject.Position position;

    public MethodCallInjection(@NonNull String inClass,
                               @NonNull String inMethodName,
                               @NonNull String inMethodDesc,
                               @NonNull String targetClass,
                               @NonNull String targetMethod,
                               @NonNull Inject.Position position) {
        super(inClass, inMethodName, inMethodDesc);

        if (position == Inject.Position.UNUSED)
            throw new IllegalArgumentException("@Inject.Position cannot be UNUSED for MethodCallInjection");

        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.position = position;
    }

    @Override
    protected void apply(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
        MethodInsnNode call = new MethodInsnNode(INVOKESTATIC, targetClass, targetMethod, "()V");

        if (position == Inject.Position.END || mtd.instructions.size() == 0)
            mtd.instructions.add(call);
        else
            mtd.instructions.insertBefore(mtd.instructions.getFirst(), call);
    }

}
