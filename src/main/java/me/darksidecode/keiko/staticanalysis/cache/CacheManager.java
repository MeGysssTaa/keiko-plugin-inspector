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

import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.formatting.Hash;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.config.InspectionsConfig;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheManager {

    private final List<InspectionCache> caches = new ArrayList<>();

    public void loadCaches() {
        File cachesFolder = getCachesFolder();
        File[] files = cachesFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
                    InspectionCache cache = CommonJson.fromJson(json, InspectionCache.class);

                    long cacheLifespanDays = InspectionsConfig.getCachesLifespanDays();
                    String installedKeikoVersion = KeikoPluginInspector.getVersion();

                    if (cache.hasExpired(cacheLifespanDays, installedKeikoVersion)) {
                        KeikoPluginInspector.debug("Skipped and deleted expired cache %s", file.getName());
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    } else {
                        KeikoPluginInspector.debug("Loaded cache %s", file.getName());
                        caches.add(cache);
                    }
                } catch (Exception ex) {
                    KeikoPluginInspector.debug("Skipped and deleted invalid cache %s", file.getName());

                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }

    public void saveCache(File file, Map<String, StaticAnalysis.Result> analysesResults) {
        String hash = hashFile(file);
        String keikoVersion = KeikoPluginInspector.getVersion();

        InspectionCache cache = new InspectionCache(
                System.currentTimeMillis(), keikoVersion, hash, analysesResults);

        File cachesFolder = getCachesFolder();
        File cacheFile = new File(cachesFolder, hash + ".dat");

        if (cacheFile.exists())
            //noinspection ResultOfMethodCallIgnored
            cacheFile.delete();

        String json = CommonJson.toJson(cache);

        try {
            FileUtils.writeStringToFile(cacheFile, json, StandardCharsets.UTF_8.name());
            KeikoPluginInspector.debug("Cached file %s as %s", file.getAbsolutePath(), hash);
        } catch (IOException ex) {
            KeikoPluginInspector.warn("Failed to cache file %s", file.getAbsolutePath());
            ex.printStackTrace();
        }
    }

    public InspectionCache getCache(File file) {
        String hash = hashFile(file);
        return caches.stream().filter(cache
                -> cache.getFileHash().equals(hash)).findFirst().orElse(null);
    }

    private String hashFile(File file) {
        return Hash.SHA256.checksumString(file).toLowerCase();
    }

    private File getCachesFolder() {
        File cachesFolder = new File(KeikoPluginInspector.getWorkDir(), ".caches/");

        //noinspection ResultOfMethodCallIgnored
        cachesFolder.mkdirs();

        return cachesFolder;
    }

}
