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
import me.darksidecode.keiko.util.References;
import me.darksidecode.keiko.util.UniqueElementsList;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

@RegisterStaticAnalysis
public class DirectLeaksAnalysis extends StaticAnalysis {

    public DirectLeaksAnalysis(@NonNull ClassNode cls) {
        super(cls);

        if (isExcluded())
            return;

        List<String> details = new UniqueElementsList<>();

        if (cls.name.startsWith("directleaks/"))
            // Their new anti-releak blatantly creates an own package with the website name.
            details.add("New anti-releak: DirectLeaks");

        if (cls.name.startsWith("de/xbrowniecodez/pluginprotect/"))
            // xBrownieCodez's encrypted message injector.
            details.add("New anti-releak: PluginProtect");

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.VULNERABLE, details));
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        if (isExcluded(mtd))
            return;

        List<String> details = new UniqueElementsList<>();

        if (References.isPrivate(mtd) && References.isStatic(mtd)
                && References.isBridge(mtd) && References.isSynthetic(mtd)
                && References.isDeprecated(mtd) && References.isNamedSuspiciously(mtd))
            // Their old anti-releak injects several methods which can easily be recognized by a Unicode
            // name and the combination of access flags [PRIVATE, STATIC, BRIDGE, SYNTHETIC, DEPRECATED].
            details.add("Old anti-releak in " + cls.name + "#" + mtd.name);

        if (!details.isEmpty())
            Keiko.INSTANCE.getStaticAnalysisManager().addResult(new StaticAnalysisResult(
                    cls, getScannerName(), StaticAnalysisResult.Type.VULNERABLE, details));
    }

}
