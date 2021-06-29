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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides some simple (though ugly and not scalable) methods that transform
 * BungeeCord classes at runtime (without modifying the original JAR) before running them.
 * This allows us to do some hacks to make Keiko fully compatible with BungeeCord.
 *
 * This class (and the way Bungee compatibility is currently handled) should definitely
 * be refactored to user some proper, scalable code. But as a "temporary" workaround it's OK.
 */
@UtilityClass
class BungeeCompatibility implements Opcodes {

    /**
     * BungeeCord uses an own, almost useless, SecurityManager.
     * Remove code that creates and sets it.
     */
    static void removeBungeeSecurityManager(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
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
            toRemove.forEach(mtd.instructions::remove);
    }

    /**
     * BungeeCord does not respect parent class loaders at all by default.
     * This causes errors (specifically, "NoClassDefFoundError" for any Bungee
     * plugins API class, or when loading transitive dependencies from other plugins).
     * This method fixes its PluginClassloader to respect KeikoClassLoader.
     */
    static void respectKeikoClassLoader(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
        AbstractInsnNode[] insnsCopy = mtd.instructions.toArray();
        AbstractInsnNode toRemove = null;

        for (AbstractInsnNode insn : insnsCopy) {
            if (insn.getOpcode() == INVOKESPECIAL) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (mtdInsn.owner.equals("java/net/URLClassLoader") && mtdInsn.name.equals("<init>")) {
                    // Replace this:
                    //
                    //         ...
                    //         INVOKESPECIAL java/net/URLClassLoader.<init> ([Ljava/net/URL;)V
                    //         ...
                    //
                    // with this:
                    //
                    //         ...
                    //         INVOKESTATIC java/lang/Thread.currentThread ()Ljava/lang/Thread;
                    //         INVOKEVIRTUAL java/lang/Thread.getContextClassLoader ()Ljava/lang/ClassLoader;
                    //         INVOKESPECIAL java/net/URLClassLoader.<init> ([Ljava/net/URL;Ljava/lang/ClassLoader;)V
                    //         ...
                    //
                    // (basically, append ", Thread.currentThread().getContextClassLoader()" to the call to "super").
                    toRemove = insn;

                    mtd.instructions.insertBefore(insn, new MethodInsnNode(
                            INVOKESTATIC, "java/lang/Thread",
                            "currentThread", "()Ljava/lang/Thread;"));

                    mtd.instructions.insertBefore(insn, new MethodInsnNode(
                            INVOKEVIRTUAL, "java/lang/Thread",
                            "getContextClassLoader", "()Ljava/lang/ClassLoader;"));

                    mtd.instructions.insertBefore(insn, new MethodInsnNode(
                            INVOKESPECIAL, "java/net/URLClassLoader",
                            "<init>", "([Ljava/net/URL;Ljava/lang/ClassLoader;)V"));
                }
            }
        }

        if (toRemove != null)
            mtd.instructions.remove(toRemove);
    }

}
