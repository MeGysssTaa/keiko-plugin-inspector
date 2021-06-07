///*
// * Copyright 2021 German Vekhorev (DarksideCode)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package me.darksidecode.keiko.pluginsintegrity;
//
//import me.darksidecode.kantanj.formatting.Hash;
//import me.darksidecode.keiko.KeikoPluginInspector;
//import me.darksidecode.keiko.config.InspectionsConfig;
//import me.darksidecode.keiko.registry.IndexedPlugin;
//
//import java.io.File;
//
//public class PluginsIntegrityChecker {
//
//    /**
//     * @return true if plugin's actual checksum matches the expected checksum set in the config
//     *         OR if this plugin is not listed in the config. Otherwise (in case the JAR of this
//     *         plugin has most likely been modified) this method returns false.
//     */
//    public boolean checkIntegrity(File file, IndexedPlugin plugin) {
//        String pluginName = plugin.getName();
//        String expectedChecksum = InspectionsConfig.getYaml().
//                getString("plugins_integrity.expected_checksums." + pluginName);
//
//        if (expectedChecksum != null) {
//            String actualChecksum = Hash.SHA256.checksumString(file);
//
//            KeikoPluginInspector.debug("Result of '%s' integrity check:", pluginName);
//            KeikoPluginInspector.debug("    Expected checksum : %s", expectedChecksum);
//            KeikoPluginInspector.debug("    Actual checksum   : %s", actualChecksum.toLowerCase());
//
//            if (!(actualChecksum.equalsIgnoreCase(expectedChecksum))) {
//                KeikoPluginInspector.warn("Integrity of plugin %s might have been violated. " +
//                                "If you updated this plugin recently, make sure to change its expected checksum " +
//                                "in Keiko/config/inspections.yml. If you didn't, then it appears that one of other " +
//                                "plugins has modified its code and possibly injected some malware - it is strongly " +
//                                "recommended that you delete %s and redownload it from official source in that case.",
//                        pluginName, pluginName);
//
//                // EXECUTED LATER (SEE JAVADOC TO METHOD me.darksidecode.keiko.staticanalysis.Countermeasures#execute):
//                //    if (InspectionsConfig.getAbortServerStartupOnIntegrityViolation())
//                //        RuntimeUtils.rageQuit();
//
//                return false; // integrity violation detected
//            }
//        }
//
//        return true; // everything looks fine
//    }
//
//}
