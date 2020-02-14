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

import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.quarantine.Quarantine;

import java.io.File;

@RequiredArgsConstructor
public enum Countermeasures {

    WARN ((analysis, jar, analysisResult) -> {
        KeikoPluginInspector.warn(
                "JAR " + jar.getName() +
                " is likely " + analysisResult.getType() +
                " (confidence: " + analysisResult.getConfidencePercent() + "%)!" +
                " Details: " + analysis.getName() + analysisResult.getDetails().toString() +
                ". It is adviced that you delete it."
        );

        return false; // don't abort server startup
    }),


    ABORT_SERVER_STARTUP ((analysis, jar, analysisResult) -> {
        synchronized (KeikoPluginInspector.outputLock) { // to avoid quitting before warning
            WARN.execute(analysis, jar, analysisResult); // ignore WARN's return result - we want to return true

            // EXECUTED LATER (SEE JAVADOC TO METHOD execute):
            //     KeikoPluginInspector.warn("The server will be shut down forcefully (rage quit).");
            //     RuntimeUtils.rageQuit();

            return true; // abort server startup
        }
    }),


    MOVE_TO_QUARANTINE ((analysis, jar, analysisResult) -> {
        synchronized (KeikoPluginInspector.outputLock) { // to avoid quitting before warning
            // executed by ABORT_SERVER_STARTUP : WARN.execute(analysis, jar, analysisResult);

            KeikoPluginInspector.warn("The aforementioned file will be moved to quarantine.");
            Quarantine.settle(analysis, analysisResult, jar);

            return ABORT_SERVER_STARTUP.execute(analysis, jar, analysisResult); // abort server startup
        }
    }),


    ;

    private final Executor executor;

    /**
     * Returns true only and only if the server startup is to be aborted
     * later (after all inspections for all plugins have been ran). This
     * allows us to print ALL warnings/errors to the user before killing
     * the server (which, in its turn, allows user to see ALL plugins that
     * were classified as malware at once, without having to restart the
     * server multiple times).
     */
    public boolean execute(StaticAnalysis analysis, File jar, StaticAnalysis.Result analysisResult) {
        try {
            return executor.execute(analysis, jar, analysisResult);
        } catch (Exception ex) {
            throw new RuntimeException("failed to execute countermeasure " + name() + " from " + analysis.getName() +
                    " for JAR " + jar.getName() + " with result " + analysisResult.toString(), ex);
        }
    }

    private interface Executor {
        boolean execute(StaticAnalysis analysis, File jar, StaticAnalysis.Result analysisResult) throws Exception;
    }

}
