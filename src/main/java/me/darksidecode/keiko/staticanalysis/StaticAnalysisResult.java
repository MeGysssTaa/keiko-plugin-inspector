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
        return Keiko.INSTANCE.getEnv().getPluginContext().getClassOwner(analyzedClassName);
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
