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

package me.darksidecode.keiko.staticanalysis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.jminima.disassembling.SimpleJavaDisassembler;
import me.darksidecode.jminima.phase.PhaseExecutionException;
import me.darksidecode.jminima.phase.basic.CloseJarFilePhase;
import me.darksidecode.jminima.phase.basic.DisassemblePhase;
import me.darksidecode.jminima.phase.basic.OpenJarFilePhase;
import me.darksidecode.jminima.phase.basic.WalkClassesPhase;
import me.darksidecode.jminima.workflow.Workflow;
import me.darksidecode.jminima.workflow.WorkflowExecutionResult;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.config.InspectionsConfig;
import me.darksidecode.keiko.i18n.I18n;
import me.darksidecode.keiko.io.UserInputRequest;
import me.darksidecode.keiko.io.YesNo;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.io.KeikoLogger;
import me.darksidecode.keiko.proxy.KeikoProperties;
import me.darksidecode.keiko.registry.Identity;
import me.darksidecode.keiko.registry.IdentityFilter;
import me.darksidecode.keiko.registry.IndexedPlugin;
import me.darksidecode.keiko.registry.PluginContext;
import me.darksidecode.keiko.staticanalysis.cache.CacheManager;
import me.darksidecode.keiko.staticanalysis.cache.InspectionCache;
import me.darksidecode.keiko.util.ConfigurationUtils;
import org.reflections.Reflections;

import java.util.*;

@RequiredArgsConstructor
public class StaticAnalysisManager {

    @NonNull
    private final PluginContext pluginContext;

    @NonNull
    private final CacheManager cacheManager;

    private final Reflections reflections = new Reflections("me.darksidecode.keiko.staticanalysis");

    private final Map<IndexedPlugin, InspectionCache> cachesToPush = new HashMap<>();

    private final Map<IndexedPlugin, List<StaticAnalysisResult>> results = new HashMap<>();

    private final Map<String, Collection<IdentityFilter>> exclusions = new HashMap<>();

    public boolean inspectAllPlugins() {
        pluginContext.getPlugins().forEach(plugin -> results.put(plugin, new ArrayList<>()));
        Keiko.INSTANCE.getLogger().infoLocalized("staticInspections.beginAll");

        try {
            cacheManager.setup();
        } catch (Exception ex) {
            Keiko.INSTANCE.getLogger().warningLocalized("staticInspections.caches.err");
            Keiko.INSTANCE.getLogger().error("Unhandled exception in: setup", ex);
        }

        // TODO: 29.06.2021 run in parallel

        for (IndexedPlugin plugin : pluginContext.getPlugins()) {
            boolean failed = inspectPlugin(plugin);

            if (failed && GlobalConfig.getAbortOnError())
                return true; // abort startup

            InspectionCache cacheToPush = cachesToPush.get(plugin);

            if (cacheToPush != null) {
                try {
                    cacheManager.push(plugin.getSha512(), cacheToPush);
                    cachesToPush.remove(plugin);
                } catch (Exception ex) {
                    Keiko.INSTANCE.getLogger().warningLocalized("staticInspections.caches.err");
                    Keiko.INSTANCE.getLogger().error("Unhandled exception in: push [%s]", plugin.getName(), ex);
                }
            }
        }

        try {
            cacheManager.tearDown();
        } catch (Exception ex) {
            Keiko.INSTANCE.getLogger().warningLocalized("staticInspections.caches.err");
            Keiko.INSTANCE.getLogger().error("Unhandled exception in: tearDown", ex);
        }

        return false; // do not abort startup
    }

    public void addResult(@NonNull StaticAnalysisResult result) {
        InspectionCache cache = cachesToPush
                .computeIfAbsent(result.getAnalyzedPlugin(),
                        k -> InspectionCache.createEmptyCache());

        List<StaticAnalysisResult> cachedResults = cache.getAnalysesResults()
                .computeIfAbsent(result.getScannerName(),
                        k -> new ArrayList<>());

        cachedResults.add(result);
        results.get(result.getAnalyzedPlugin()).add(result);
    }

    public boolean processResults() {
        int warningsTotal = printResultsAndCountWarnings();

        for (List<StaticAnalysisResult> list : results.values()) {
            for (StaticAnalysisResult result : list) {
                String countermeasuresType = result.getType().name().toLowerCase();
                String configScannerName = StaticAnalysis.inspectionNameToConfigName(result.getScannerName());
                String countermeasuresString = InspectionsConfig.getHandle()
                        .get(configScannerName + ".countermeasures." + countermeasuresType);

                Countermeasures countermeasures = Countermeasures.fromString(countermeasuresString);

                if (countermeasures.getAbortStartupFunc().apply(result))
                    return true; // yes, abort startup
            }
        }

        if (warningsTotal > 0) { // ask user whether to abort startup or not // IMPORTANT: negate result (see below)
            if (KeikoProperties.staticInspWarnsYes != null)
                // System startup property set. Just use its predefined result and don't prompt anything.
                return !KeikoProperties.staticInspWarnsYes;
            else {
                // Message like "Continue anyway? [yes/no]"
                String prompt = I18n.get("staticInspections.proceedAnywayPrompt")
                        + " [" + I18n.get("prompts.yes") + "/" + I18n.get("prompts.no") + "]";

                // Prompt user to enter "yes" or "no" explicitly.
                return !UserInputRequest.newBuilder(System.in, YesNo.class)
                        .prompt(Keiko.INSTANCE.getLogger(), prompt)
                        .lineTransformer(String::trim)
                        .build()
                        .block()
                        .toBoolean(); // TRUE = user wants the server to start, FALSE = user wants the startup to abort
            }
        }

        return false; // no, do not abort startup
    }

    public boolean isExcluded(@NonNull StaticAnalysis inspection, @NonNull Identity identity) {
        return getExclusions(inspection).stream()
                .anyMatch(exclusion -> exclusion.matches(identity));
    }

    private Collection<IdentityFilter> getExclusions(StaticAnalysis inspection) {
        String scannerName = inspection.getScannerName();

        return exclusions.computeIfAbsent(scannerName, k -> // lazy get
                ConfigurationUtils.getExclusionsList(InspectionsConfig.getHandle(),
                        StaticAnalysis.inspectionNameToConfigName(scannerName) + ".exclusions"));
    }

    private int printResultsAndCountWarnings() {
        int warningsTotal = 0, criticalTotal = 0;

        for (IndexedPlugin plugin : results.keySet()) {
            List<StaticAnalysisResult> pluginResults = results.get(plugin);
            int warnings = 0, critical = 0;

            for (StaticAnalysisResult result : pluginResults) {
                if (result.getType() != StaticAnalysisResult.Type.CLEAN    ) warnings++;
                if (result.getType() == StaticAnalysisResult.Type.MALICIOUS) critical++;
            }

            warningsTotal += warnings;
            criticalTotal += critical;

            // Log level depends on whether there was at least a single warning or not.
            // If the plugin is fully clean, then just debug it. Otherwise really warn.
            KeikoLogger.Level logLevel = warnings == 0
                    ? KeikoLogger.Level.DEBUG : KeikoLogger.Level.WARNING;

            Keiko.INSTANCE.getLogger().logLocalized(
                    logLevel, "staticInspections.pluginResults",
                    plugin.getName(), plugin.getJar().getName());

            for (StaticAnalysisResult result : pluginResults) {
                String typeI18nKey = "staticInspections." + result.getType().name().toLowerCase();
                String analysisDescI18nKey = "staticInspections.desc." + result.getScannerName();

                Keiko.INSTANCE.getLogger().logLocalized(logLevel, typeI18nKey);
                Keiko.INSTANCE.getLogger().logLocalized(
                        logLevel, "staticInspections.analysisName", result.getScannerName());
                Keiko.INSTANCE.getLogger().logLocalized(
                        logLevel, analysisDescI18nKey, plugin.getName(), plugin.getJar().getName());
                Keiko.INSTANCE.getLogger().logLocalized(logLevel, "staticInspections.details");

                int counter = 0;

                for (String detail : result.getDetails())
                    Keiko.INSTANCE.getLogger().log(logLevel, "|            %s. %s", ++counter, detail);

                Keiko.INSTANCE.getLogger().log(logLevel, "|            ");
            }

            Keiko.INSTANCE.getLogger().logLocalized(
                    logLevel, "staticInspections.pluginSummary",
                    warnings, critical, plugin.getName(), plugin.getJar().getName());

            Keiko.INSTANCE.getLogger().log(logLevel, " ");
            Keiko.INSTANCE.getLogger().log(logLevel, " ");
        }

        Keiko.INSTANCE.getLogger().infoLocalized(
                "staticInspections.finishSummary", warningsTotal, criticalTotal);

        return warningsTotal;
    }

    private boolean inspectPlugin(IndexedPlugin plugin) {
        return runInspectionWorkflow(plugin, collectInspections(plugin)); // true = failed, false = succeeded
    }

    private Collection<Class<? extends StaticAnalysis>> collectInspections(IndexedPlugin plugin) {
        Collection<Class<? extends StaticAnalysis>> inspections = new ArrayList<>();

        for (Class<?> inspectionClass : reflections.getTypesAnnotatedWith(RegisterStaticAnalysis.class)) {
            if (!StaticAnalysis.class.isAssignableFrom(inspectionClass))
                throw new RuntimeException("illegal StaticAnalysis " +
                        "(annotated but invalid type): " + inspectionClass.getName());

            String inspectionName = StaticAnalysis
                    .classToInspectionName((Class<? extends StaticAnalysis>) inspectionClass);
            String configName = StaticAnalysis.inspectionNameToConfigName(inspectionName);

            if (InspectionsConfig.getHandle().get(configName + ".enabled", false)) {
                try {
                    String inputJarPath = plugin.getJar().getAbsolutePath()
                            .replace("\\", "/"); // better Windows compatibility
                    String pluginsFolderPath = Keiko.INSTANCE.getEnv().getPluginsDir().getAbsolutePath();
                    List<String> exclusions = InspectionsConfig.getHandle()
                            .get(configName + ".exclusions", Collections.emptyList());

                    boolean excluded = false;

                    for (String exclusion : exclusions) {
                        if (exclusion.replace("{plugins_folder}", pluginsFolderPath)
                                .replace("\\", "/") // better Windows compatibility
                                .equals(inputJarPath)) {
                            excluded = true;
                            break;
                        }
                    }

                    if (!excluded)
                        inspections.add((Class<? extends StaticAnalysis>) inspectionClass);
                } catch (Exception ex) {
                    throw new RuntimeException("invalid managed inspection: " + inspectionClass.getName(), ex);
                }
            }
        }

        return inspections;
    }

    private boolean runInspectionWorkflow(IndexedPlugin plugin,
                                          Collection<Class<? extends StaticAnalysis>> inspections) {
        InspectionCache cache = null;

        try {
            cache = cacheManager.fetch(plugin.getSha512());
        } catch (Exception ex) {
            Keiko.INSTANCE.getLogger().warningLocalized("staticInspections.caches.err");
            Keiko.INSTANCE.getLogger().error("Unhandled exception in: fetch [%s]", plugin.getName(), ex);
        }

        if (cache == null)
            cache = InspectionCache.createEmptyCache();

        InspectionCache finalCache = cache;
        Map<String, List<StaticAnalysisResult>> cachedAnalysesResults = finalCache.getAnalysesResults();

        try (Workflow workflow = new Workflow()
                .phase(new OpenJarFilePhase(plugin.getJar()))
                .phase(new DisassemblePhase(SimpleJavaDisassembler.class))) {
            int cached = 0;

            for (Class<? extends StaticAnalysis> inspection : inspections) {
                String inspectionName = StaticAnalysis.classToInspectionName(inspection);
                List<StaticAnalysisResult> cachedResults = cachedAnalysesResults.get(inspectionName);

                if (cachedResults != null) {
                    Keiko.INSTANCE.getLogger().debugLocalized(
                            "staticInspections.caches.resultCached",
                            inspectionName, plugin.getName(), plugin.getJar().getName(), cachedResults.size());

                    results.get(plugin).addAll(cachedResults);
                    cached++;
                } else {
                    Keiko.INSTANCE.getLogger().debugLocalized(
                            "staticInspections.caches.resultNotCached",
                            inspectionName, plugin.getName(), plugin.getJar().getName());

                    workflow.phase(new WalkClassesPhase(inspection));
                    cachesToPush
                            .computeIfAbsent(plugin, k -> finalCache)
                            .getAnalysesResults()
                            .put(inspectionName, new ArrayList<>());
                }
            }

            workflow.phase(new CloseJarFilePhase());

            if (cached != inspections.size()) {
                // At least one inspection has not been cached. Let's fix it!
                WorkflowExecutionResult result = workflow.executeAll();

                if (result != WorkflowExecutionResult.FULL_SUCCESS) {
                    int isFatal = result == WorkflowExecutionResult.FATAL_FAILURE
                            ? 1  // true
                            : 0; // false

                    Keiko.INSTANCE.getLogger().warningLocalized("staticInspections.err",
                            isFatal, plugin.getName(), plugin.getJar().getName());

                    for (PhaseExecutionException error : workflow.getAllErrorsChronological())
                        Keiko.INSTANCE.getLogger().error("JMinima phase execution error:", error);

                    return true; // inspection failed
                }
            }

            Keiko.INSTANCE.getLogger().debugLocalized(
                    "staticInspections.caches.analysisStats",
                    inspections.size(), plugin.getName(), plugin.getJar().getName(), cached);

            return false; // inspection succeeded
        }
    }

}
