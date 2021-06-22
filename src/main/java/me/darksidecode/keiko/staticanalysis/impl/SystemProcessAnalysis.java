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
import java.util.List;

@RegisterStaticAnalysis
public class SystemProcessAnalysis extends StaticAnalysis {

    public SystemProcessAnalysis(@NonNull ClassNode cls) {
        super(cls);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        List<String> details = new ArrayList<>();

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);
            int op = insn.getOpcode();

            if (op == NEW) {
                TypeInsnNode typeInsn = (TypeInsnNode) insn;

                if (typeInsn.desc.contains("java/lang/ProcessBuilder"))
                    // Direct "new ProcessBuilder(...)" usage.
                    details.add("ProcessBuilder creation in " + cls.name + "#" + mtd.name);
            } else if (op == INVOKESPECIAL || op == INVOKEVIRTUAL || op == INVOKESTATIC) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (mtdInsn.owner.contains("java/lang/ProcessBuilder"))
                    // Anything related to ProcessBuilder is potentially unsafe.
                    details.add("ProcessBuilder usage in " + cls.name + "#" + mtd.name);
                else if (mtdInsn.owner.contains("java/lang/Runtime") && mtdInsn.name.equals("exec"))
                    // Direct "Runtime.getRuntime().exec(...)" usage.
                    details.add("Use of the Runtime class " +
                            "for system command invocation in " + cls.name + "#" + mtd.name);
            }
        }

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.SUSPICIOUS, details));
    }

}
