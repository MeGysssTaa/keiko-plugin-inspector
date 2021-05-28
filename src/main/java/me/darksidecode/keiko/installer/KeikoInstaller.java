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

package me.darksidecode.keiko.installer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

@RequiredArgsConstructor
public class KeikoInstaller {

    @NonNull
    private final File keikoJar, pluginsFolder, workDir;

    public File checkInstallation(String path) {
        return checkInstallation(new File(workDir, path), path);
    }

    public File checkInstallation(File file, String internalPath) {
        if (!(file.exists())) {
            try {
                InputStream is = internalResource(internalPath);
                FileOutputStream fos = new FileOutputStream(file);

                IOUtils.copy(is, fos);

                is.close();
                fos.close();
            } catch (Exception ex) {
                throw new RuntimeException("failed to check installation; " +
                        "local: " + file.getAbsolutePath() + "; internal: " + internalPath, ex);
            }
        }

        return file;
    }

    public InputStream internalResource(String name) {
        return Objects.requireNonNull(getClass().getClassLoader().
                getResourceAsStream(name), "unrecognized internal resource: " + name);
    }

}
