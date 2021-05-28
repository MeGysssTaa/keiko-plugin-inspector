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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

@ManagedInspection (
        name = "Static.CodeInjection",
        countermeasuresForSuspicious = Countermeasures.WARN,
        countermeasuresForMalicious = Countermeasures.ABORT_SERVER_STARTUP
)
public class CodeInjectionAnalysis extends StaticAnalysis {

    private static final int SUSPICIOUS_SCORE = 35;
    private static final int MALICIOUS_SCORE  = 70;

    private final List<String> detections = new ArrayList<>();

    private double score;

    public CodeInjectionAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        super(name, inputJarName, classes);
    }

    @Override
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception {
        for (int i = 0; i < mtdNode.instructions.size(); i++) {
            AbstractInsnNode insn = mtdNode.instructions.get(i);
            int op = insn.getOpcode();

            if (op == INVOKESTATIC || op == INVOKEVIRTUAL) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (mtdInsn.owner.equals(CLASS_LOADER_NAME)) {
                    score += 20;
                    detections.add("detected direct ClassLoader class usage " +
                            "in method " + mtdNode.name + " declared in class " + clsNode.name);
                } else if (mtdInsn.owner.startsWith("java/security/")) {
                    score += 25;
                    detections.add("detected direct java.security package usage " +
                            "in method " + mtdNode.name + " declared in class " + clsNode.name);
                } else if (mtdInsn.owner.startsWith("javassist/")) {
                    score += 35;
                    detections.add("detected direct Javassist library usage " +
                            "in method " + mtdNode.name + " declared in class " + clsNode.name);
                } else if (mtdInsn.owner.startsWith("org/objectweb/asm/")) {
                    score += 35;
                    detections.add("detected direct ASM library usage " +
                            "in method " + mtdNode.name + " declared in class " + clsNode.name);
                }
            }
        }

        return null;
    }

    @Override
    protected Result analyzeEnd() throws Exception {
        if (score > 100.0)
            score = 100.0;

        if (score >= SUSPICIOUS_SCORE) {
            Result.Type type = score >= MALICIOUS_SCORE
                    ? Result.Type.MALICIOUS : Result.Type.SUSPICIOUS;

            List<String> details = new ArrayList<>();

            details.add("Detected code that can arbitrarily modify the behavior of other plugins or " +
                    "installed applications. Such code can be used to inject malware in your existing " +
                    "plugins, or even in the server core.");

            details.addAll(detections);

            return new Result(type, score, details);
        }

        return null;
    }

}
