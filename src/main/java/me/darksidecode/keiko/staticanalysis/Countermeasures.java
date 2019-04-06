/*
 * Copyright 2019 DarksideCode
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

import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.quarantine.Quarantine;
import me.darksidecode.keiko.util.RuntimeUtils;

import java.io.File;

@RequiredArgsConstructor
public enum Countermeasures {

    WARN ((analysis, jar, analysisResult) ->
        KeikoPluginInspector.warn(
                "JAR " + jar.getName() +
                " is likely " + analysisResult.getType() +
                " (confidence: " + analysisResult.getConfidencePercent() + "%)!" +
                " Details: " + analysis.getName() + analysisResult.getDetails().toString() +
                ". It is adviced that you delete it."
        )
    ),


    ABORT_SERVER_STARTUP ((analysis, jar, analysisResult) -> {
        synchronized (KeikoPluginInspector.outputLock) { // to avoid quitting before warning
            WARN.execute(analysis, jar, analysisResult);

            KeikoPluginInspector.warn("The server will be shut down forcefully (rage quit).");
            RuntimeUtils.rageQuit();
        }
    }),


    MOVE_TO_QUARANTINE ((analysis, jar, analysisResult) -> {
        synchronized (KeikoPluginInspector.outputLock) { // to avoid quitting before warning
            // executed by ABORT_SERVER_STARTUP : WARN.execute(analysis, jar, analysisResult);

            KeikoPluginInspector.warn("The aforementioned file will be moved to quarantine.");
            Quarantine.settle(analysis, analysisResult, jar);

            ABORT_SERVER_STARTUP.execute(analysis, jar, analysisResult);
        }
    }),


    ;

    private final Executor executor;

    public void execute(StaticAnalysis analysis, File jar, StaticAnalysis.Result analysisResult) {
        try {
            executor.execute(analysis, jar, analysisResult);
        } catch (Exception ex) {
            throw new RuntimeException("failed to execute countermeasure " + name() + " from " + analysis.getName() +
                    " for JAR " + jar.getName() + " with result " + analysisResult.toString(), ex);
        }
    }

    private interface Executor {
        void execute(StaticAnalysis analysis, File jar, StaticAnalysis.Result analysisResult) throws Exception;
    }

}
