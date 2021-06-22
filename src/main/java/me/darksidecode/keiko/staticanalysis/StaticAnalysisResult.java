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
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.registry.IndexedPlugin;
import org.objectweb.asm.tree.ClassNode;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class StaticAnalysisResult implements Serializable {

    private static final long serialVersionUID = 2209625722066232934L;

    @Getter
    private final String analyzedClassName;

    @Getter
    private final String scannerName;

    @Getter
    private final Type type;

    @Getter
    private final List<String> details;

    public StaticAnalysisResult(@NonNull ClassNode analyzedClass, @NonNull String scannerName,
                                @NonNull Type type, @NonNull List<String> details) {
        this.analyzedClassName = analyzedClass.name.replace("/", ".");
        this.scannerName = scannerName;
        this.type = type;
        this.details = details;
    }
    
    public IndexedPlugin getAnalyzedPlugin() {
        return Keiko.INSTANCE.getPluginContext().getClassOwner(analyzedClassName);
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
        return analyzedClassName.equals(that.analyzedClassName)
                && scannerName.equals(that.scannerName)
                && type == that.type
                && details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analyzedClassName, scannerName, type, details);
    }

}
