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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.jminima.walking.ClassWalker;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.registry.Identity;
import me.darksidecode.keiko.registry.IndexedPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public abstract class StaticAnalysis implements ClassWalker {

    @NonNull
    protected final ClassNode cls;

    @Override
    public final boolean hasModifiedAnything() {
        return false;
    }

    @Override
    public void visitClass() {}

    @Override
    public void visitField(@NonNull FieldNode fld) {}

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {}

    protected final boolean isExcluded() {
        return isExcluded(null);
    }

    protected final boolean isExcluded(MethodNode mtd) {
        return Keiko.INSTANCE.getStaticAnalysisManager()
                .isExcluded(this, identity(cls, mtd));
    }

    protected final String getScannerName() {
        return classToInspectionName(getClass());
    }

    private static Identity identity(@NonNull ClassNode cls, MethodNode mtd) {
        String className = cls.name.replace('/', '.');
        IndexedPlugin plugin = Keiko.INSTANCE.getPluginContext().getClassOwner(className);

        return new Identity(
                plugin.getJar().getAbsolutePath(),
                plugin.getName(),
                className,
                mtd != null ? mtd.name : null
        );
    }

    public static String classToInspectionName(@NonNull Class<? extends StaticAnalysis> c) {
        return "Static." + c.getSimpleName().replace("Analysis", "");
    }

    public static String inspectionNameToConfigName(@NonNull String s) {
        char[] nameChars = s.replace("Static.", "").toCharArray();
        StringBuilder configNameBuilder = new StringBuilder();
        boolean firstChar = true;

        for (char c : nameChars) {
            if (firstChar)
                firstChar = false;
            else if (Character.isUpperCase(c))
                configNameBuilder.append('_');

            configNameBuilder.append(Character.toLowerCase(c));
        }

        return "static." + configNameBuilder;
    }

}
