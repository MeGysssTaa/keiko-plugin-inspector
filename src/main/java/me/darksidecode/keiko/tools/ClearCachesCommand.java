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

import me.darksidecode.keiko.util.RuntimeUtils;

import java.io.File;

class ClearCachesCommand extends Command {

    ClearCachesCommand() {
        super("clear-caches", "Delete all Keiko static inspections' caches.",
                "clear-caches", 0);
    }

    @Override
    protected void execute(String[] args) throws Exception {
        File pluginsFolder = RuntimeUtils.getSourceJar(ClearCachesCommand.class).getParentFile();

        if ((!(pluginsFolder.isDirectory())) || (!(pluginsFolder.getName().equals("plugins"))))
            throw new IllegalStateException("keiko-tools JAR must " +
                    "be Keiko plugin JAR and placed inside the server's plugins/ folder");

        File keikoFolder = new File(pluginsFolder, "Keiko/");

        if (!(keikoFolder.isDirectory()))
            throw new IllegalStateException(
                    "Keiko folder does not exist (" + keikoFolder.getAbsolutePath() + ")");

        File cachesFolder = new File(keikoFolder, ".caches/");

        int totalFilesDeleted = 0;
        long totalBytesDeleted = 0;

        if (cachesFolder.isDirectory()) {
            File[] caches = cachesFolder.listFiles();

            if (caches != null) {
                for (File cacheFile : caches) {
                    totalFilesDeleted++;
                    totalBytesDeleted += cacheFile.length();

                    //noinspection ResultOfMethodCallIgnored
                    cacheFile.delete();
                }
            }
        }

        if (totalFilesDeleted == 0)
            System.out.println("No caches deleted. Perhaps there are not any.");
        else
            System.out.printf("Deleted %d cache files (%d bytes in total).\n", totalFilesDeleted, totalBytesDeleted);
    }

}
