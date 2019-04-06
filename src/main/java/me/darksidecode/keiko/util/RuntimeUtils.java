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

package me.darksidecode.keiko.util;

import me.darksidecode.kantanj.logging.BasicLogger;
import me.darksidecode.kantanj.system.Shell;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.registry.IndexedPlugin;
import me.darksidecode.keiko.runtimeprotect.CallerInfo;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Objects;

public final class RuntimeUtils {

    private RuntimeUtils() {}

    public static File getSourceJar(Class clazz) {
        try {
            return new File(Objects.requireNonNull(clazz, "clazz cannot be null").
                    getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception ex) {
            throw new RuntimeException("failed to get source jar for class: " + clazz.getName(), ex);
        }
    }

    public static CallerInfo getCallerInfo() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement e : stacktrace) {
            String callerClassName = e.getClassName();
            String callerMethodName = e.getMethodName();

            IndexedPlugin plugin = KeikoPluginInspector.
                    getPluginContext().getClassOwner(callerClassName);

            if ((plugin != null) && (!(plugin.getJar().equals(KeikoPluginInspector.getKeikoJar()))))
                return new CallerInfo(plugin, callerClassName, callerMethodName);
        }

        return null;
    }

    public static void rageQuit() {
        if (GlobalConfig.getAllowKeikoRageQuit()) {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();

            if (jvmName.contains("@")) {
                String pidStr = jvmName.split("@")[0].trim();

                if (pidStr.replaceAll("[0-9]", "").isEmpty()) { // just digits - probably our PID
                    KeikoPluginInspector.warn("Sending SIGKILL(9) to process %s (rage quit)", pidStr);
                    Shell.execute("Keiko Rage Quit", KeikoPluginInspector.getWorkDir(),
                            "kill -9 " + pidStr, new BasicLogger() {});
                } else
                    Bukkit.shutdown();
            } else
                Bukkit.shutdown();
        } else
            Bukkit.shutdown();
    }

}
