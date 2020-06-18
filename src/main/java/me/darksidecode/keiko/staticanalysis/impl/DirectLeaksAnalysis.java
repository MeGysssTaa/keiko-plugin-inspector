/*
 * Copyright 2020 DarksideCode
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
import me.darksidecode.keiko.util.References;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collection;

@ManagedInspection (
        name = "Static.DirectLeaks",
        countermeasuresForSuspicious = Countermeasures.ABORT_SERVER_STARTUP,
        countermeasuresForMalicious = Countermeasures.ABORT_SERVER_STARTUP
)
public class DirectLeaksAnalysis extends StaticAnalysis {

    private static final String DETECTION_MESSAGE =
            "detected a pirated copy of a premium plugin. " +
            "Leaks are often spoofy and contain malicious code. " +
            "By using cracked plugins you put your server in danger, whereas " +
            "by purchasing genuine software you support the developers and get " +
            "the ability to report bugs, request help, and receive regular updates";

    public DirectLeaksAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        super(name, inputJarName, classes);
    }

    @Override
    protected Result analyzeClass(ClassNode clsNode) throws Exception {
        // Their new anti-releak blatantly creates an own package with the website name
        if (clsNode.name.startsWith("directleaks/"))
            return new Result(Result.Type.MALICIOUS, 100.0,
                    Arrays.asList(DETECTION_MESSAGE, "Detected new anti-releak."));

        return null;
    }

    @Override
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception {
        // Their old anti-releak injects several methods which can easily be recognized by a Unicode
        // name and the combination of access flags [PRIVATE, STATIC, BRIDGE, SYNTHETIC, DEPRECATED].
        if (References.isPrivate(mtdNode) && References.isStatic(mtdNode)
                && References.isBridge(mtdNode) && References.isSynthetic(mtdNode)
                && References.isDeprecated(mtdNode) && References.isNamedSuspiciously(mtdNode))
            return new Result(Result.Type.MALICIOUS, 100.0,
                    Arrays.asList(DETECTION_MESSAGE, "Detected old anti-releak."));

        return null;
    }

}
