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

package me.darksidecode.keiko.NEW_staticanalysis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.jminima.walking.ClassWalker;
import me.darksidecode.keiko.KeikoPluginInspector;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public class StaticAnalysis implements ClassWalker {

    @NonNull
    private final ClassNode cls;

    @Override
    public boolean hasModifiedAnything() {
        return false;
    }

    @Override
    public void visitClass() {
        KeikoPluginInspector.info("CLASS %s", cls.name);
    }

    @Override
    public void visitField(@NonNull FieldNode fld) {
        KeikoPluginInspector.info("    FIELD %s", fld.name);
    }

    @Override
    public void visitMethod(@NonNull MethodNode mtd) {
        KeikoPluginInspector.info("    METHOD %s", mtd.name);
    }

}
