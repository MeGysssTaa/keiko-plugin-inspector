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

package me.darksidecode.keiko.runtimeprotect;

import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.runtimeprotect.dac.KeikoSecurityManager;
import org.bukkit.Bukkit;

public class RuntimeProtect {

    public void setupDomainAccessControl() {
        KeikoPluginInspector.debug("Setting up Keiko security manager");

        if ((System.getSecurityManager() != null)
                || (System.getProperty("java.security.manager") != null)
                || (System.getProperty("java.security.policy") != null)) {
            KeikoPluginInspector.warn("JVM security manager is already set. " +
                    "Domain access control may not work properly. Check your start command arguments for: " +
                    "'java.security.manager', 'java.security.policy'");

            Bukkit.shutdown();

            return;
        }

        SecurityManager mgr = new KeikoSecurityManager();
        System.setSecurityManager(mgr);
    }

}
