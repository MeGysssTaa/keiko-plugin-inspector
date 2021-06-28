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
                mtd != null ? mtd.name : ""
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
