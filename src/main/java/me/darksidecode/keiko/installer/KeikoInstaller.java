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

package me.darksidecode.keiko.installer;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

@RequiredArgsConstructor
public class KeikoInstaller {

    @Getter @NonNull
    private final File workDir;

    public File checkInstallation(@NonNull String path) {
        return checkInstallation(new File(workDir, path), path);
    }

    public File checkInstallation(@NonNull File file, @NonNull String internalPath) {
        if (!file.exists()) {
            try (InputStream      in  = internalResource(internalPath);
                 FileOutputStream out = new FileOutputStream(file)   ) {
                IOUtils.copy(in, out);
            } catch (Exception ex) {
                throw new RuntimeException("failed to check installation; " +
                        "local: " + file.getAbsolutePath() + "; internal: " + internalPath, ex);
            }
        }

        return file;
    }

    public InputStream internalResource(@NonNull String name) {
        return Objects.requireNonNull(getClass().getClassLoader().
                getResourceAsStream(name), "unrecognized internal resource: " + name);
    }

}
