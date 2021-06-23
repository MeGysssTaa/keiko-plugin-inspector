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
import me.darksidecode.keiko.util.UniqueElementsList;
import org.objectweb.asm.tree.*;

import java.util.List;

@RegisterStaticAnalysis
public class ForceOpAnalysis extends StaticAnalysis {

    private boolean hasOpLdcBefore;

    public ForceOpAnalysis(@NonNull ClassNode cls) {
        super(cls);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        if (isExcluded(mtd))
            return;

        List<String> details = new UniqueElementsList<>();
        hasOpLdcBefore = false; // reset

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);
            inspectBukkitApi(mtd, insn, details);
            inspectConsoleCommand(mtd, insn, details);
        }

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.MALICIOUS, details));
    }

    private void inspectBukkitApi(MethodNode mtd, AbstractInsnNode insn, List<String> details) {
        if (insn.getOpcode() == INVOKEINTERFACE) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;
            String mtdOwner = mtdInsn.owner;

            boolean oppableEntity =
                       mtdOwner.equals("org/bukkit/permissions/ServerOperator")
                    || mtdOwner.equals("org/bukkit/entity/Player")
                    || mtdOwner.equals("org/bukkit/entity/HumanEntity")
                    || mtdOwner.equals("org/bukkit/OfflinePlayer")
                    || mtdOwner.equals("org/bukkit/command/CommandSender");

            if (oppableEntity && mtdInsn.name.equals("setOp"))
                // Direct (explicit) Player#setOp usage.
                details.add("Direct (explicit) Bukkit setOp API usage in " + cls.name + "#" + mtd.name);
        }
    }

    private void inspectConsoleCommand(MethodNode mtd, AbstractInsnNode insn, List<String> details) {
        if (insn.getOpcode() == LDC) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
            Object cst = ldcInsn.cst;

            if (cst instanceof String) {
                String stringCst = ((String) cst).trim().toLowerCase();

                if (stringCst.startsWith("op ") || stringCst.startsWith("deop "))
                    // Indicate that a potential `op`/`deop` command was seen recently.
                    hasOpLdcBefore = true;
            }
        } else if (hasOpLdcBefore
                && (insn.getOpcode() == INVOKESTATIC || insn.getOpcode() == INVOKEINTERFACE)) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;

            if (mtdInsn.name.equals("dispatchCommand")
                    && (mtdInsn.owner.equals("org/bukkit/Bukkit")
                     || mtdInsn.owner.equals("org/bukkit/Server")))
                // `Bukkit.dispatchCommand` or `Bukkit.getServer()#dispatchCommand` (or something similar
                // that retrieves current Server object and invokes `dispatchCommand`) usage with a command
                // that appears to contain force-op/deop calls.
                details.add("Console command usage for op/deop in " + cls.name + mtd.name);
        }
    }

}
