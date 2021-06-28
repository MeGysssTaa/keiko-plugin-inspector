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

import java.util.List;

@RegisterStaticAnalysis
public class SystemProcessAnalysis extends StaticAnalysis {

    public SystemProcessAnalysis(@NonNull ClassNode cls) {
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
