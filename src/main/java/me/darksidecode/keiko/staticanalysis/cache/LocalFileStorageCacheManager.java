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
