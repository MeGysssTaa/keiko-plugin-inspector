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

package me.darksidecode.keiko.proxy.injector;

import lombok.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Getter (AccessLevel.PACKAGE)
@RequiredArgsConstructor (access = AccessLevel.PACKAGE)
class Injection implements Opcodes {

    @NonNull
    private final String inClass    , inMethod    , // where the call is injected
                         targetClass, targetMethod; // call to which method is injected

    @NonNull
    private final Inject.Position position;

    private boolean applied;

    void apply(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
        MethodInsnNode call = generateInjectionCall();

        if (position == Inject.Position.END || mtd.instructions.size() == 0)
            mtd.instructions.add(call);
        else
            mtd.instructions.insertBefore(mtd.instructions.getFirst(), call);

        applied = true;
    }

    private MethodInsnNode generateInjectionCall() {
        return new MethodInsnNode(INVOKESTATIC, targetClass, targetMethod, "()V");
    }

}
