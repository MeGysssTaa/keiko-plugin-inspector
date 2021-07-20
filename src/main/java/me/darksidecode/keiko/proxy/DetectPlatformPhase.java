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

import java.util.jar.JarFile;

public class DetectPlatformPhase extends Phase<JarFile, Platform> {

    @Override
    public Class<? super JarFile> getTargetTypeClass() {
        return JarFile.class;
    }

    @Override
    protected EmittedValue<? extends Platform> execute(JarFile target,
                                                       PhaseExecutionException error) throws Throwable {
        if (target == null)
            return new EmittedValue<>(new PhaseExecutionException(
                    true, "failed to detect platform from the given target data", error));

        Platform[] supportedPlatforms = Platform.values();
        Platform inferredPlatform = null;

        for (Platform platform : supportedPlatforms) {
            if (target.getEntry(platform.getReferenceFile()) != null) {
                // A reference file is present in the proxied JAR.
                if (inferredPlatform == null)
                    // We've just found a reference file, and there are no other reference files in the proxied JAR.
                    inferredPlatform = platform;
                else {
                    // There are other reference files in the proxied JAR as well. Something's wrong.
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            "startup.ambiguousPlatform", inferredPlatform, platform);
                    inferredPlatform = Platform.BUKKIT; // fallback to Bukkit (generally more common and safe)
                }
            }
        }

        if (inferredPlatform == null) {
            Keiko.INSTANCE.getLogger().warningLocalized("startup.unsupportedPlatform");
            System.exit(1);
            return null;
        }

        Keiko.INSTANCE.getLogger().debugLocalized("startup.platform", inferredPlatform);

        return new EmittedValue<>(inferredPlatform);
    }

}
