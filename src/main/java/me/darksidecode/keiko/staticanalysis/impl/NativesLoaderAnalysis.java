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

package me.darksidecode.keiko.staticanalysis.impl;

import me.darksidecode.keiko.staticanalysis.Countermeasures;
import me.darksidecode.keiko.staticanalysis.ManagedInspection;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.Collections;

@ManagedInspection (
        name = "Static.NativesLoader",
        countermeasuresForSuspicious = Countermeasures.WARN, // Keiko is capable of blocking natives linkage at runtime
        countermeasuresForMalicious = Countermeasures.WARN // --^

)
public class NativesLoaderAnalysis extends StaticAnalysis {

    public NativesLoaderAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        super(name, inputJarName, classes);
    }

    @Override
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception {
        for (int i = 0; i < mtdNode.instructions.size(); i++) {
            AbstractInsnNode insn = mtdNode.instructions.get(i);
            int op = insn.getOpcode();

            if ((op == INVOKESTATIC) || (op == INVOKEVIRTUAL)) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (((mtdInsn.owner.equals(SYSTEM_NAME)) || (mtdInsn.owner.equals(RUNTIME_NAME)))
                        && ((mtdInsn.name.equals("load")) || (mtdInsn.name.equals("loadLibrary"))))
                    return new Result(Result.Type.SUSPICIOUS, 50.0, Collections.singletonList(
                            "detected natives linkage using #" + mtdInsn.name
                                    + " in method " + mtdNode.name + " declared in class " + clsNode.name));
            }
        }

        return null;
    }

}
