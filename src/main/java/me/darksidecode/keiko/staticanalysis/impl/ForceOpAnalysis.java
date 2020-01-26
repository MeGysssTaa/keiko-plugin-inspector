/*
 * Copyright 2020 DarksideCode
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

import me.darksidecode.keiko.staticanalysis.Countermeasures;
import me.darksidecode.keiko.staticanalysis.ManagedInspection;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.Collections;

@ManagedInspection (
        name = "Static.ForceOp",
        countermeasuresForSuspicious = Countermeasures.ABORT_SERVER_STARTUP,
        countermeasuresForMalicious = Countermeasures.ABORT_SERVER_STARTUP
)
public class ForceOpAnalysis extends StaticAnalysis {

    private boolean hasOpLdcBefore;

    public ForceOpAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        super(name, inputJarName, classes);
    }

    @Override
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception {
        hasOpLdcBefore = false; // reset
        Result result;

        for (int i = 0; i < mtdNode.instructions.size(); i++) {
            AbstractInsnNode insn = mtdNode.instructions.get(i);

            if ((result = inspectBukkitApi(clsNode, mtdNode, insn)) != null)
                return result;

            if ((result = inspectConsoleCommand(clsNode, mtdNode, insn)) != null)
                return result;
        }

        return null;
    }

    private Result inspectBukkitApi(ClassNode clsNode, MethodNode mtdNode, AbstractInsnNode insn) {
        if (insn.getOpcode() == INVOKEINTERFACE) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;
            String mtdOwner = mtdInsn.owner;

            boolean oppableEntity = mtdOwner.equals(PLAYER_NAME)
                    || mtdOwner.equals(OFFLINE_PLAYER_NAME)
                    || mtdOwner.equals(COMMAND_SENDER_NAME)
                    || mtdOwner.equals(HUMAN_ENTITY_NAME);

            if ((oppableEntity) && (mtdInsn.name.equals("setOp")))
                // Blatant Player#setOp usage.
                return new Result(Result.Type.MALICIOUS, 100.0, Collections.singletonList(
                        "detected OP-giving Bukkit API usage in method " + mtdNode.name +
                                " declared in class " + clsNode.name));
        }

        return null;
    }

    private Result inspectConsoleCommand(ClassNode clsNode, MethodNode mtdNode, AbstractInsnNode insn) {
        if (insn.getOpcode() == LDC) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insn;
            Object cst = ldcInsn.cst;

            if (cst instanceof String) {
                String stringCst = ((String) cst).trim().toLowerCase();

                if ((stringCst.contains("op ")) || (stringCst.contains("deop ")))
                    // Indicate that a potential op/deop command has been found recently.
                    hasOpLdcBefore = true;
            }
        } else if ((hasOpLdcBefore)
                && ((insn.getOpcode() == INVOKESTATIC) || (insn.getOpcode() == INVOKEINTERFACE))) {
            MethodInsnNode mtdInsn = (MethodInsnNode) insn;

            if ((mtdInsn.name.equals("dispatchCommand"))
                    && ((mtdInsn.owner.equals(BUKKIT_NAME)) || (mtdInsn.owner.equals(SERVER_NAME))))
                // `Bukkit.dispatchCommand` or `Bukkit.getServer()#dispatchCommand` (or something similar
                // that retrieves current Server object and invokes `dispatchCommand` usage with a command
                // that appears to contain force-op/deop calls.
                return new Result(Result.Type.MALICIOUS, 100.0, Collections.singletonList(
                        "detected OP-giving command invocation using dispatchCommand in method " + mtdNode.name +
                                " declared in class " + clsNode.name));
        }

        return null;
    }

}
