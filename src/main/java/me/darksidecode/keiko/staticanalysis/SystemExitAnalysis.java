/*
 * Copyright 2019 DarksideCode
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

package me.darksidecode.keiko.staticanalysis;

import me.darksidecode.keiko.util.References;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.Collections;

@ManagedInspection (
        name = "Static.SystemExit",
        countermeasuresForSuspicious = Countermeasures.WARN, // Keiko is capable of blocking system exiting at runtime
        countermeasuresForMalicious = Countermeasures.WARN // --^
)
public class SystemExitAnalysis extends StaticAnalysis {

    SystemExitAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        super(name, inputJarName, classes);
    }

    @Override
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception {
        for (int i = 0; i < mtdNode.instructions.size(); i++) {
            AbstractInsnNode insn = mtdNode.instructions.get(i);

            if (insn.getOpcode() == INVOKESTATIC) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if ((mtdInsn.owner.equals(References.transformedClassName(System.class)))
                        && (mtdInsn.name.equals("exit")))
                    return new Result(Result.Type.SUSPICIOUS, 50.0, Collections.singletonList(
                            "detected unsafe System.exit usage in method " + mtdNode.name
                                    + " declared in class " + clsNode.name));

            } else if (insn.getOpcode() == INVOKEVIRTUAL) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if ((mtdInsn.owner.equals(References.transformedClassName(Runtime.class)))
                        && (mtdInsn.name.equals("exit")))
                    return new Result(Result.Type.SUSPICIOUS, 50.0, Collections.singletonList(
                            "detected unsafe Runtime.getRuntime().exit usage in method " + mtdNode.name
                                    + " declared in class " + clsNode.name));
            }
        }

        return null;
    }

}
