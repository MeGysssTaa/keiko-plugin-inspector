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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

@RegisterStaticAnalysis
public class CodeInjectionAnalysis extends StaticAnalysis {

    private static final int SUSPICIOUS_SCORE = 50;
    private static final int MALICIOUS_SCORE  = 75;

    private int score;

    public CodeInjectionAnalysis(@NonNull ClassNode cls) {
        super(cls);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        if (isExcluded(mtd))
            return;

        List<String> details = new UniqueElementsList<>();

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);
            int op = insn.getOpcode();

            if (op == INVOKESTATIC || op == INVOKEVIRTUAL) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (mtdInsn.owner.equals("java/lang/ClassLoader")) {
                    score += 5;
                    details.add("ClassLoader API usage in " + cls.name + "#" + mtd.name);
                } else if (mtdInsn.owner.startsWith("javassist/")) {
                    score += 25;
                    details.add("JavaAssist library usage in " + cls.name + "#" + mtd.name);
                } else if (mtdInsn.owner.startsWith("org/objectweb/asm/")) {
                    score += 25;
                    details.add("ASM library usage in " + cls.name + "#" + mtd.name);
                }
            }
        }

        if (score >= SUSPICIOUS_SCORE && !details.isEmpty()) {
            if (score > 100)
                score = 100;

            details.add("== Score: " + score);

            StaticAnalysisResult.Type resultType;

            if (score >= MALICIOUS_SCORE)
                resultType = StaticAnalysisResult.Type.MALICIOUS;
            else
                resultType = StaticAnalysisResult.Type.SUSPICIOUS;

            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), resultType, details));
        }
    }

}
