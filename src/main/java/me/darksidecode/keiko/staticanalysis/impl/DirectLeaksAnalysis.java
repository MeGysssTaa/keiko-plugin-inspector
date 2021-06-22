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
import me.darksidecode.keiko.util.References;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

@RegisterStaticAnalysis
public class DirectLeaksAnalysis extends StaticAnalysis {

    public DirectLeaksAnalysis(@NonNull ClassNode cls) {
        super(cls);

        List<String> details = new ArrayList<>();

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
        List<String> details = new ArrayList<>();

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
