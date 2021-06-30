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

package me.darksidecode.keiko.proxy.injector.injection.bungee;


import lombok.NonNull;
import me.darksidecode.keiko.proxy.injector.Inject;
import me.darksidecode.keiko.proxy.injector.injection.Injection;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * BungeeCord uses an own, almost useless, SecurityManager.
 * Remove code that creates and sets it.
 */
@Inject (
        inClass = "net.md_5.bungee.BungeeCord",
        inMethod = "<init>()V"
)
public class BungeeCordInjection extends Injection {

    public BungeeCordInjection(String inClass, String inMethodName, String inMethodDesc) {
        super(inClass, inMethodName, inMethodDesc);
    }

    @Override
    protected void apply(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
           /*

              linenumber      192
         307: new             Lnet/md_5/bungee/BungeeSecurityManager;
         310: dup
         311: invokespecial   net/md_5/bungee/BungeeSecurityManager.<init>:()V
         314: invokestatic    java/lang/System.setSecurityManager:(Ljava/lang/SecurityManager;)V

         */
        List<AbstractInsnNode> toRemove = null;

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);

            if (insn.getOpcode() == INVOKESTATIC) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (mtdInsn.owner.equals("java/lang/System") && mtdInsn.name.equals("setSecurityManager")) {
                    // Remove this instruction, and four instructions above it, which all basically form the line:
                    //
                    //         Line 192        System.setSecurityManager(new BungeeSecurityManager());
                    //
                    // (don't bind to the line number strictly, though!).
                    toRemove = new ArrayList<>();

                    for (int j = i - 4; j <= i; j++)
                        toRemove.add(mtd.instructions.get(j));

                    break; // we don't need to check any more instructions
                }
            }
        }

        if (toRemove != null)
            // Remove all the instructions we found.
            toRemove.forEach(mtd.instructions::remove);
    }

}
