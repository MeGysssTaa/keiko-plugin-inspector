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

import me.darksidecode.keiko.staticanalysis.Countermeasures;
import me.darksidecode.keiko.staticanalysis.ManagedInspection;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Collection;

@ManagedInspection (
        name = "Static.SystemProcess",
        countermeasuresForSuspicious = Countermeasures.ABORT_SERVER_STARTUP,
        countermeasuresForMalicious = Countermeasures.ABORT_SERVER_STARTUP
)
public class SystemProcessAnalysis extends StaticAnalysis {

    public SystemProcessAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        super(name, inputJarName, classes);
    }

    @Override
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception {
        for (int i = 0; i < mtdNode.instructions.size(); i++) {
            AbstractInsnNode insn = mtdNode.instructions.get(i);
            int op = insn.getOpcode();

            if (op == NEW) {
                TypeInsnNode typeInsn = (TypeInsnNode) insn;

                if (typeInsn.desc.contains(PROCESS_BUILDER_NAME))
                    return new Result(Result.Type.MALICIOUS, 100.0, Arrays.asList(
                            "Detected unsafe system/SSH command usage in method "
                                    + mtdNode.name + " declared in class " + clsNode.name
                                    + " (hidden malicious SSH access?)",
                            "ProcessBuilder creation"));
            } else if ((op == INVOKESPECIAL) || (op == INVOKEVIRTUAL) || (op == INVOKESTATIC)) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                // We need a special condition for kantanj's Shell class because its located inside
                // the Keiko jar. That is, hackers may try to bypass the ordinary ProcessBuilder/Runtime.exec
                // analysis by just using the method Keiko uses itself (Shell#execute).
                if ((mtdInsn.owner.equals(PROCESS_BUILDER_NAME))
                        || ((mtdInsn.owner.equals(RUNTIME_NAME)) && (mtdInsn.name.equals("exec")))
                        || ((mtdInsn.owner.equals(SHELL_NAME)) && (mtdInsn.name.equals("execute"))))
                    return new Result(Result.Type.MALICIOUS, 100.0, Arrays.asList(
                            "Detected unsafe system/SSH command usage in method "
                                    + mtdNode.name + " declared in class " + clsNode.name
                                    + " (hidden malicious SSH access?)",
                            "Runtime#exec/Shell#execute call"));
            }
        }

        return null;
    }

}
