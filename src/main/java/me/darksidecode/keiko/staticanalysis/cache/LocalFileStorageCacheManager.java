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

package me.darksidecode.keiko.staticanalysis.cache;

import lombok.NonNull;
import me.darksidecode.kantanj.system.FileUtils;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.util.JsonFileUtils;

import java.io.File;

public class LocalFileStorageCacheManager implements CacheManager {

    private static final String CACHE_FILE_EXTENSION = ".dat";

    @Override
    public InspectionCache fetch(@NonNull String sha512) throws Exception {
        File cacheFile = getCacheFile(sha512);
        InspectionCache result = null;

        if (cacheFile.isFile()) {
            InspectionCache cache = JsonFileUtils.readCompressedJsonUtf8(cacheFile, InspectionCache.class);
            String installedKeikoVersion = Keiko.INSTANCE.getBuildProperties().getVersion();

            if (installedKeikoVersion.equals(cache.getKeikoVersion()))
                result = cache;
            else
                //noinspection ResultOfMethodCallIgnored
                cacheFile.delete();
        }

        return result;
    }

    @Override
    public boolean push(@NonNull String sha512, @NonNull InspectionCache cache) throws Exception {
        File cacheFile = getCacheFile(sha512);

        if (cacheFile.exists() && !cacheFile.delete())
            return false; // failed to delete cache file that already exists

        JsonFileUtils.writeCompressedJsonUtf8(cacheFile, cache, FileUtils.OverwriteMode.OVERWRITE);

        return true;
    }

    private File getCacheFile(String sha512) {
        return new File(getCachesFolder(), sha512 + CACHE_FILE_EXTENSION);
    }

    private File getCachesFolder() {
        File cachesFolder = new File(Keiko.INSTANCE.getWorkDir(), ".artifacts/static-inspections-caches/");
        //noinspection ResultOfMethodCallIgnored
        cachesFolder.mkdirs();

        return cachesFolder;
    }

}
