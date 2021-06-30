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

/**
 * BungeeCord does not respect parent class loaders at all by default.
 * This causes errors (namely, "NoClassDefFoundError" for any Bungee plugins
 * API class, or when loading transitive dependencies from other plugins).
 * This injection fixes its PluginClassloader to respect KeikoClassLoader.
 */
@Inject (
        inClass = "net.md_5.bungee.api.plugin.PluginClassloader",
        inMethod = "<init>(" +
                "Lnet/md_5/bungee/api/ProxyServer;" +
                "Lnet/md_5/bungee/api/plugin/PluginDescription;" +
                "Ljava/io/File;" +
                "Ljava/lang/ClassLoader;" +
                ")V"
)
public class PluginClassloaderInjection extends Injection {

    public PluginClassloaderInjection(String inClass, String inMethodName, String inMethodDesc) {
        super(inClass, inMethodName, inMethodDesc);
    }

    @Override
    protected void apply(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
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
                    // (basically, append ", Thread.currentThread().getContextClassLoader()" to the call to <init>).
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
            // Don't forget to remove the original "bad" call!
            mtd.instructions.remove(toRemove);
    }

}
