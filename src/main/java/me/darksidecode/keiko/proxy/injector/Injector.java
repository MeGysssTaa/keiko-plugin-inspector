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

package me.darksidecode.keiko.proxy.injector;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.jminima.disassembling.JavaDisassembler;
import me.darksidecode.jminima.phase.EmittedValue;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor
public class Injector {

    @NonNull
    private final JarFile jar;

    @NonNull
    private final JavaDisassembler disassembler;

    private final InjectionsCollector collector = new InjectionsCollector();

    public long getAppliedInjections() {
        return collector.getAppliedInjections();
    }

    public long getSkippedInjections() {
        return collector.getSkippedInjections();
    }

    public byte[] inject(@NonNull JarEntry entry) throws ClassNotFoundException, InjectionException {
        // Check whether we have anything to inject into this class or not.
        String className = entry.getName()
                .replace(".class/", "")
                .replace(".class", "");

        if (collector.collectInjections(className, null, null).isEmpty()) {
            // We have nothing to inject in this class. Read its bytes as is from the JAR.
            // This is faster than blindly disassembling and reassembling all classes with ASM.
            try (InputStream inputStream = jar.getInputStream(entry)) {
                return IOUtils.toByteArray(inputStream);
            } catch (IOException ex) {
                throw new ClassNotFoundException(className, ex);
            }
        }

        // We have something to inject in this class.
        try {
            // Disassemble.
            EmittedValue<? extends ClassNode> result = disassembler.disassemble(entry);

            if (result.getError() != null)
                throw new InjectionException(
                        "failed to disassemble jar entry " + entry.getName(), result.getError());

            // Inject.
            ClassNode cls = result.getValue();

            if (cls.methods != null) {
                for (MethodNode mtd : cls.methods) {
                    checkBungeeTransform(cls, mtd); // TODO: 29.06.2021 awful; replace with sth proper & scalable
                    Collection<Injection> injections = collector
                            .collectInjections(cls.name, mtd.name, mtd.desc);
                    injections.forEach(injection -> injection.apply(cls, mtd));
                }
            }

            // Reassemble.
            ClassWriter cw = new ClassWriter(0);
            cls.accept(cw);

            return cw.toByteArray();
        } catch (Exception ex) {
            throw new InjectionException("unhandled exception during injection", ex);
        }
    }
    
    private void checkBungeeTransform(ClassNode cls, MethodNode mtd) {
        // BungeeCord uses an own, almost useless, SecurityManager.
        // Remove code that creates and sets it (just at runtime).
        if (cls.name.equals("net/md_5/bungee/BungeeCord") && mtd.name.equals("<init>"))
            BungeeSecMgrRemover.apply(cls, mtd);
    }

}
