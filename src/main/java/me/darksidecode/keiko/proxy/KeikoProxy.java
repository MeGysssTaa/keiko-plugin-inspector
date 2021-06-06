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

package me.darksidecode.keiko.proxy;

import java.io.File;
import java.lang.reflect.Method;

public final class KeikoProxy {
    
    private KeikoProxy() {}

    public static final KeikoProxy INSTANCE = new KeikoProxy();

    private volatile boolean started;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Please specify the JAR file for Keiko to proxy.");
            System.err.println("For details, see the installations instructins at:");
            System.err.println("https://github.com/MeGysssTaa/keiko-plugin-inspector/wiki/Installation-Instructions");
            System.exit(1);
            
            return;
        }

        File proxiedExecutable = new File(String.join(" ", args));

        if (!proxiedExecutable.exists()) {
            System.err.println("The specified JAR file does not exist.");
            System.exit(1);

            return;
        }

        if (!proxiedExecutable.exists()) {
            System.err.println("The specified path is a directory, not a file.");
            System.exit(1);

            return;
        }

        if (!proxiedExecutable.canRead()) {
            System.err.println("The specified JAR file cannot be read (no permissions?).");
            System.exit(1);

            return;
        }

        INSTANCE.start(proxiedExecutable);
    }

    private void start(File proxiedExecutable) throws Exception {
        if (started)
            throw new IllegalStateException("cannot start twice");

        started = true;
        System.out.println("Setting up Keiko proxy, please wait...");

        KeikoClassLoader loader = new KeikoClassLoader(proxiedExecutable);
        Thread.currentThread().setContextClassLoader(loader);
        System.out.printf("Loaded %d classes (%d non-fatal errors)\n",
                loader.getLoadResult().successes, loader.getLoadResult().failures);

        System.out.println("Launching " + loader.getBootstrapClassName());
        Class<?> bootstrapClass = loader.findClass(loader.getBootstrapClassName());
        Method bootstrapMethod = bootstrapClass.getMethod("main", String[].class);
        bootstrapMethod.invoke(null, (Object) new String[0]);

        System.out.println("The process Keiko was proxying has terminated.");
    }

}
