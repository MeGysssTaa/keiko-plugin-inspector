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

package me.darksidecode.keiko.integrity;

import lombok.NonNull;
import me.darksidecode.kantanj.system.FileUtils;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.util.JsonFileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class ChecksumsCacheFile {

    private static final String CACHE_FILE_EXTENSION = ".dat";

    Map<String, String> read() {
        File file = getFile();

        try {
            return JsonFileUtils.readCompressedJsonUtf8(file, HashMap.class);
        } catch (Exception ex) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return new HashMap<>();
        }
    }

    void save(@NonNull Map<String, String> checksums) {
        JsonFileUtils.writeCompressedJsonUtf8(getFile(), checksums, FileUtils.OverwriteMode.OVERWRITE);
    }

    private File getFile() {
        File artifactsFolder = new File(Keiko.INSTANCE.getWorkDir(), ".artifacts/");
        //noinspection ResultOfMethodCallIgnored
        artifactsFolder.mkdirs();

        return new File(artifactsFolder, "checksums-cache" + CACHE_FILE_EXTENSION);
    }

}
