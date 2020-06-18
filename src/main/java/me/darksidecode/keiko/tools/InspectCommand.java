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

package me.darksidecode.keiko.tools;

import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.staticanalysis.StaticAnalysisManager;

import java.io.File;

class InspectCommand extends Command {

    InspectCommand() {
        super("inspect", "Run static inspections on a particular plugin or on all plugins at once.",
                "inspect [target jar file]", 0);
    }

    @Override
    protected void execute(String[] args) throws Exception {
        StaticAnalysisManager manager = new StaticAnalysisManager();

        if (args.length > 0) { // inspect one particular plugin
            // Use StringBuilder to allow spaces in file/directory names.
            StringBuilder pathBuilder = new StringBuilder();

            for (String arg : args)
                pathBuilder.append(arg).append(' ');

            pathBuilder.deleteCharAt(pathBuilder.length() - 1); // delete trailing space
            String path = pathBuilder.toString();

            File targetJarFile = path.contains("plugins/")
                    ? new File(pathBuilder.toString())
                    : new File(KeikoPluginInspector.getPluginsFolder(), path);

            if (targetJarFile.isFile()) {
                KeikoPluginInspector.info("Scanning file %s...", targetJarFile.getAbsolutePath());
                manager.analyzeJar(targetJarFile);
            } else
                KeikoPluginInspector.warn("File not found: %s", targetJarFile.getAbsolutePath());
        } else { // inspect all plugins at once
            KeikoPluginInspector.info("Running static analysis in folder %s. " +
                    "This may take some time...", KeikoPluginInspector.getPluginsFolder().getAbsolutePath());
            KeikoPluginInspector.warn("Keiko will not check the " +
                    "integrity of installed plugins in standalone mode!");

            File[] files = KeikoPluginInspector.getPluginsFolder().listFiles();

            if (files != null) {
                for (File file : files) {
                    // Unlike in Keiko itself, we don't require the files to be ".jar".
                    // This is to allow scanning disabled plugins (e.g. ".jard" as well).
                    if (file.isFile()) {
                        // Scan each file in an own try-catch so that an exception inspecting one
                        // file will not result in all other files being skipped from the analysis.
                        try {
                            manager.analyzeJar(file);
                        } catch (Exception ex) {
                            KeikoPluginInspector.warn("Failed to inspect file %s:", file.getAbsolutePath());
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
