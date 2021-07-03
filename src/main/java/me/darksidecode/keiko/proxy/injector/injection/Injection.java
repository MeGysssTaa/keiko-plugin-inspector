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

package me.darksidecode.keiko.proxy.injector.injection;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.proxy.Keiko;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Injection is a base class for any manipulations performed with bytecode before adding it to the classpath.
 * It may delete or change code, or keep everything as is in some situations.
 */
@RequiredArgsConstructor
public abstract class Injection implements Opcodes {

    /**
     * Where the injection is applied.
     */
    @Getter
    protected final String inClass, inMethodName, inMethodDesc;

    /**
     * Has this injection been applied successfully?
     */
    @Getter
    protected boolean applied;

    public final void applyAndRecord(@NonNull ClassNode cls, @NonNull MethodNode mtd) {
        if (!applied) { // avoid duplicate application
            apply(cls, mtd);

            Keiko.INSTANCE.getLogger().debug("Applied injection %s to %s#%s%s",
                    getClass().getName(), inClass, inMethodName, inMethodDesc);

            applied = true;
        }
    }

    protected abstract void apply(@NonNull ClassNode cls, @NonNull MethodNode mtd);

}
