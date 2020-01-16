/*
 * Copyright 2020 DarksideCode
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

package me.darksidecode.keiko.staticanalysis;

import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.config.InspectionsConfig;
import me.darksidecode.keiko.staticanalysis.cache.CacheManager;
import me.darksidecode.keiko.staticanalysis.cache.InspectionCache;
import me.darksidecode.keiko.util.Bytecoding;
import org.objectweb.asm.tree.ClassNode;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public class StaticAnalysisManager {

    private final CacheManager cacheManager;

    public StaticAnalysisManager() {
        (cacheManager = new CacheManager()).loadCaches();
    }

    public void analyzeJar(File inputJar) {
        InspectionCache cache = cacheManager.getCache(inputJar);
        Map<String, StaticAnalysis.Result> caches;

        if (cache != null) {
            KeikoPluginInspector.debug("Successfully loaded caches " +
                    "for file %s (%s)", inputJar.getAbsolutePath(), cache.getFileHash());
            caches = cache.getAnalysesResults();
        } else {
            KeikoPluginInspector.info("File %s has not been inspected for a long period of time " +
                    "or at all. It may take slight time to analyze it...", inputJar.getAbsolutePath());
            caches = new HashMap<>();
        }

        String inputJarName = inputJar.getName();
        Collection<ClassNode> classes = Bytecoding.loadClasses(inputJar).values();
        Reflections reflections = new Reflections("me.darksidecode.keiko.staticanalysis");

        for (Class inspectionClass : reflections.getTypesAnnotatedWith(ManagedInspection.class)) {
            if (!(StaticAnalysis.class.isAssignableFrom(inspectionClass)))
                throw new RuntimeException("illegal managed inspection " +
                        "(annotated but invalid type): " + inspectionClass.getName());

            ManagedInspection miAnno = (ManagedInspection)
                    inspectionClass.getAnnotation(ManagedInspection.class);

            String name = miAnno.name();
            String configName = name.toLowerCase().
                    replace(".", "_").
                    replace("static_", "static.");

            if (InspectionsConfig.getYaml().getBoolean(configName + ".enabled", true)) {
                try {
                    List<String> exclusions = InspectionsConfig.getYaml().
                            getStringList(configName + ".exclusions");

                    if (exclusions.contains(inputJar.getAbsolutePath())) {
                        KeikoPluginInspector.debug(
                                "JAR %s is excluded from analysis %s in config.", inputJarName, name);
                        continue;
                    }

                    Constructor<StaticAnalysis> constructor = inspectionClass.
                            getDeclaredConstructor(String.class, String.class, Collection.class);
                    constructor.setAccessible(true);

                    StaticAnalysis analysis = constructor.newInstance(name, inputJarName, classes);
                    StaticAnalysis.Result result = caches.get(analysis.getName());

                    if (result == null) {
                        // Not cached.
                        result = analysis.run();
                        caches.put(analysis.getName(), result);

                        // Update or create caches for this file.
                        //
                        // We don't do this outside/after this `for` loop because the process
                        // may be forcibly terminated in processResult (ABORT_SERVER_STARTUP).
                        cacheManager.saveCache(inputJar, caches);
                    } else
                        KeikoPluginInspector.debug("%s result for %s is cached: %s",
                                name, inputJarName, result);

                    processResult(inputJar, inputJarName, name, configName, analysis, result, miAnno);
                } catch (Exception ex) {
                    throw new RuntimeException("invalid managed " +
                            "inspection: " + inspectionClass.getName(), ex);
                }
            }
        }
    }

    private void processResult(File inputJar, String inputJarName, String name, String configName,
                               StaticAnalysis analysis, StaticAnalysis.Result result, ManagedInspection miAnno) {
        KeikoPluginInspector.debug("[Static Analysis] [%s] %s: %s",
                name, inputJarName, result.toString());

        if (result.getType() != StaticAnalysis.Result.Type.ALL_CLEAN) {
            Countermeasures countermeasures = result.getRecommendedCountermeasures();
            Countermeasures typeDefaultCountermeasures =
                    (result.getType() == StaticAnalysis.Result.Type.MALICIOUS)
                            ? miAnno.countermeasuresForMalicious() : miAnno.countermeasuresForSuspicious();

            if (countermeasures == null)
                countermeasures = typeDefaultCountermeasures;

            String configuredCountermeasures = InspectionsConfig.getYaml().getString(
                    configName + ".overwrite_countermeasures", "");

            KeikoPluginInspector.debug("Countermeasures: recommended: %s, type-default: %s, " +
                            "configured: '%s'", result.getRecommendedCountermeasures(),
                    typeDefaultCountermeasures, configuredCountermeasures);

            if ((configuredCountermeasures != null) && (!(configuredCountermeasures.isEmpty()))) {
                try {
                    configuredCountermeasures = configuredCountermeasures.
                            trim().toUpperCase().replace(" ", "_");
                    countermeasures = Countermeasures.valueOf(configuredCountermeasures);
                } catch (IllegalArgumentException ex) {
                    KeikoPluginInspector.warn("Configuration syntax error: invalid value at " +
                            "overwrite_countermeasures for inspection " + name + ": " +
                            configuredCountermeasures + "; expected either empty ('') or one of: " +
                            Arrays.toString(Countermeasures.values()) + ". Falling back to default " +
                            "(recommended) value: " + countermeasures.name());
                }
            }

            KeikoPluginInspector.debug("Executing countermeasures: %s", countermeasures);
            countermeasures.execute(analysis, inputJar, result);
        }
    }

}
