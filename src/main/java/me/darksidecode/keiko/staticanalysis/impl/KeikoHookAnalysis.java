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
public class KeikoHookAnalysis extends StaticAnalysis {

    public KeikoHookAnalysis(@NonNull ClassNode cls) {
        super(cls);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        if (isExcluded(mtd))
            return;

        List<String> details = new UniqueElementsList<>();

        for (int i = 0; i < mtd.instructions.size(); i++) {
            AbstractInsnNode insn = mtd.instructions.get(i);

            if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mtdInsn = (MethodInsnNode) insn;

                if (mtdInsn.owner.startsWith("me/darksidecode/keiko"))
                    // Explicit use of Keiko classes. Almost 100% an attempt to "hack" into it.
                    details.add("Direct (explicit) use of Keiko internal class "
                            + mtdInsn.owner + " in " + cls.name + "#" + mtd.name);
            } else if (insn.getOpcode() == LDC) {
                LdcInsnNode ldcInsn = (LdcInsnNode) insn;

                if (ldcInsn.cst instanceof String) {
                    String stringCst = ((String) ldcInsn.cst)
                            .trim().toLowerCase().replace('.', '/');

                    if (stringCst.startsWith("me/darksidecode/keiko"))
                        // Explicit use of Keiko classes through reflection. Almost 100% an attempt to "hack" into it.
                        details.add("Possible reflective use of Keiko internal class "
                                + stringCst + " (as " + ldcInsn.cst + ") in " + cls.name + "#" + mtd.name);
                }
            }
        }

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.MALICIOUS, details));
    }

}
