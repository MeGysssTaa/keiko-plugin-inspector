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
