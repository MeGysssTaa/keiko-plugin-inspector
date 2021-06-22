/*
 * Copyright 2021 German Vekhorev (DarksideCode)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.darksidecode.keiko.staticanalysis.impl;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.staticanalysis.RegisterStaticAnalysis;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;
import me.darksidecode.keiko.staticanalysis.StaticAnalysisResult;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RegisterStaticAnalysis
public class LuckPermsAbuseAnalysis extends StaticAnalysis {

    private static final String[] LUCKPERMS_COMMANDS_PREFIXES = {
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

    public LuckPermsAbuseAnalysis(@NonNull ClassNode cls) {
        super(cls);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        List<String> details = new ArrayList<>();
        hasPermLdcBefore = false; // reset

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);
            inspectLuckPermsApi(mtd, insn, details);
            inspectConsoleCommand(mtd, insn, details);
        }

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.SUSPICIOUS, details));
    }

    private void inspectLuckPermsApi(MethodNode mtd, AbstractInsnNode insn, List<String> details) {
        if (insn.getOpcode() == INVOKEINTERFACE) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;

            if (mtdInsn.owner.equals("me/lucko/luckperms/api/manager/UserManager"))
                details.add("Direct (explicit) use of " +
                        "LuckPerms UserManager class in " + cls.name + "#" + mtd.name);

            if (mtdInsn.owner.equals("me/lucko/luckperms/api/caching/UserData")
                    || mtdInsn.owner.equals("me/lucko/luckperms/api/caching/GroupData")
                    || mtdInsn.owner.equals("me/lucko/luckperms/api/caching/CachedData"))
                details.add("Direct (explicit) use of " +
                        "LuckPerms CachedData class in " + cls.name + "#" + mtd.name);
        }
    }

    private void inspectConsoleCommand(MethodNode mtd, AbstractInsnNode insn, List<String> details) {
        if (insn.getOpcode() == LDC) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
            Object cst = ldcInsn.cst;

            if (cst instanceof String) {
                String stringCst = ((String) cst).trim().toLowerCase();

                if (Arrays.stream(LUCKPERMS_COMMANDS_PREFIXES)
                        .anyMatch(prefix -> stringCst.startsWith(prefix + " ")))
                    // Indicate that a potential LuckPerms command was seen recently.
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
                // that appears to contain LuckPerms interaction calls.
                details.add("Console command usage for LuckPerms interaction in " + cls.name + mtd.name);
        }
    }

}
