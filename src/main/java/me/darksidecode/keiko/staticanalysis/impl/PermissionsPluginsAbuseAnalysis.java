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

package me.darksidecode.keiko.staticanalysis.impl;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.staticanalysis.RegisterStaticAnalysis;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;
import me.darksidecode.keiko.staticanalysis.StaticAnalysisResult;
import me.darksidecode.keiko.util.UniqueElementsList;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.List;

@RegisterStaticAnalysis
public class PermissionsPluginsAbuseAnalysis extends StaticAnalysis {

    private static final String[] PERMISSIONS_PLUGINS_COMMANDS = {
            // PermissionsEx
            "permissionsex",
            "pex",
            "promote",
            "demote",
            // LuckPerms
            "lp",
            "luckperms",
            "lpb",
            "luckpermsbungee",
            "lpv",
            "luckpermsvelocity",
            "permissions",
            "perms",
            "perm"
    };

    private boolean hasPermLdcBefore;

    public PermissionsPluginsAbuseAnalysis(@NonNull ClassNode cls) {
        super(cls);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        if (isExcluded(mtd))
            return;

        List<String> details = new UniqueElementsList<>();
        hasPermLdcBefore = false; // reset

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);
            inspectPluginsApi(mtd, insn, details);
            inspectConsoleCommand(mtd, insn, details);
        }

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.SUSPICIOUS, details));
    }

    private void inspectPluginsApi(MethodNode mtd, AbstractInsnNode insn, List<String> details) {
        if (insn.getOpcode() == INVOKEINTERFACE || insn.getOpcode() == INVOKEVIRTUAL) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;

            if (mtdInsn.owner.startsWith("ru/tehkode/permissions/")
                    || mtdInsn.owner.startsWith("ca/stellardrift/permissionsex/"))
                // PermissionsEx
                details.add("Direct (explicit) interaction with PermissionsEx in "
                        + cls.name + "#" + mtd.name
                        + " (invokes " + mtdInsn.owner + "#" + mtdInsn.name + ")");
            else if (mtdInsn.owner.startsWith("me/lucko/luckperms/"))
                // LuckPerms
                details.add("Direct (explicit) interaction with LuckPerms in "
                        + cls.name + "#" + mtd.name
                        + " (invokes " + mtdInsn.owner + "#" + mtdInsn.name + ")");
            else if (mtdInsn.owner.startsWith("net/milkbowl/vault/permission/"))
                // Vault (permissions)
                details.add("Direct (explicit) interaction with Vault (permissions) in "
                        + cls.name + "#" + mtd.name
                        + " (invokes " + mtdInsn.owner + "#" + mtdInsn.name + ")");
        }
    }

    private void inspectConsoleCommand(MethodNode mtd, AbstractInsnNode insn, List<String> details) {
        if (insn.getOpcode() == LDC) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
            Object cst = ldcInsn.cst;

            if (cst instanceof String) {
                String stringCst = ((String) cst).trim().toLowerCase();

                if (Arrays.stream(PERMISSIONS_PLUGINS_COMMANDS)
                        .anyMatch(prefix -> stringCst.startsWith(prefix + " ")))
                    // Indicate that a potential permissions plugin command was seen recently.
                    hasPermLdcBefore = true;
            }
        } else if (hasPermLdcBefore
                && (insn.getOpcode() == INVOKESTATIC || insn.getOpcode() == INVOKEINTERFACE)) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;

            if (mtdInsn.name.equals("dispatchCommand")
                    && (mtdInsn.owner.equals("org/bukkit/Bukkit")
                    || mtdInsn.owner.equals("org/bukkit/Server")))
                // `Bukkit.dispatchCommand` or `Bukkit.getServer()#dispatchCommand` (or something similar
                // that retrieves current Server object and invokes `dispatchCommand`) usage with a command
                // that appears to contain permissions command calls.
                details.add("Console command usage for permissions plugin interaction in " + cls.name + "#" + mtd.name);
        }
    }

}
