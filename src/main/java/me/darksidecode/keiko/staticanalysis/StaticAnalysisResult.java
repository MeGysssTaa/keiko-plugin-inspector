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

package me.darksidecode.keiko.staticanalysis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.registry.IndexedPlugin;
import org.objectweb.asm.tree.ClassNode;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class StaticAnalysisResult implements Serializable {

    private static final long serialVersionUID = 2209625722066232934L;

    @NonNull
    private final ClassNode analyzedClass;

    @NonNull
    private final String scannerName;

    @NonNull
    private final Type type;

    @NonNull
    private final List<String> details;

    public IndexedPlugin getAnalyzedPlugin() {
        return Keiko.INSTANCE.getPluginContext().getClassOwner(
                analyzedClass.name.replace("/", "."));
    }

    public enum Type {
        CLEAN,      // almost certainly not malware
        VULNERABLE, // uses unsafe code, opens potential vulnerabilities
        SUSPICIOUS, // could possibly act as malware
        MALICIOUS   // almost certainly malware
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticAnalysisResult)) return false;
        StaticAnalysisResult that = (StaticAnalysisResult) o;
        return cname().equals(that.cname())
                && scannerName.equals(that.scannerName)
                && type == that.type
                && details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cname(), scannerName, type, details);
    }

    private String cname() {
        return analyzedClass.outerClass + "-->" + analyzedClass.nestHostClass + "-->" + analyzedClass.name;
    }

}
