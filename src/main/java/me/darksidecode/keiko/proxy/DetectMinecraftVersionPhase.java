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

package me.darksidecode.keiko.proxy;

import me.darksidecode.jminima.phase.EmittedValue;
import me.darksidecode.jminima.phase.Phase;
import me.darksidecode.jminima.phase.PhaseExecutionException;

import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DetectMinecraftVersionPhase extends Phase<JarFile, String> {

    @Override
    public Class<? super JarFile> getTargetTypeClass() {
        return JarFile.class;
    }

    @Override
    protected EmittedValue<? extends String> execute(JarFile target,
                                                     PhaseExecutionException error) throws Exception {
        if (target == null)
            return new EmittedValue<>(new PhaseExecutionException(
                    true, "failed to detect Minecraft server version from the given target data", error));

        Optional<JarEntry> someNmsEntry = target.stream()
                .filter(entry -> !entry.isDirectory()
                        && entry.getName().startsWith("net/minecraft/server/"))
                .findAny();

        if (someNmsEntry.isPresent()) {
            String nmsVersion = someNmsEntry.get().getName().split("/")[3]; // infer from package name
            Keiko.INSTANCE.getLogger().debug("Minecraft server: %s", nmsVersion);

            return new EmittedValue<>(nmsVersion);
        } else
            return new EmittedValue<>(new PhaseExecutionException(
                    false, "no net.minecraft.server classes found"));
    }

}
