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

import me.darksidecode.kantanj.formatting.Hash;
import me.darksidecode.keiko.util.RuntimeUtils;

import java.io.File;

class ChecksumCommand extends Command {

    ChecksumCommand() {
        super("checksum", "Print SHA-256 checksum of the specified file from plugins folder.",
                "checksum <name of the needed JAR file name from plugins folder>", 1);
    }

    @Override
    protected void execute(String[] args) throws Exception {
        String fileName = args[0].trim();

        try {
            File pluginsFolder = RuntimeUtils.getSourceJar(ChecksumCommand.class).getParentFile();

            if ((!(pluginsFolder.isDirectory())) || (!(pluginsFolder.getName().equals("plugins"))))
                throw new IllegalStateException("keiko-tools JAR must " +
                        "be Keiko plugin JAR and placed inside the server's plugins/ folder");

            File pluginFile = new File(pluginsFolder, fileName);
            String checksum = Hash.SHA256.checksumString(pluginFile).toLowerCase();

            System.out.println("SHA-256 checksum of file " + fileName + ":");
            System.out.println(checksum);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            System.err.println("Are you sure the specified file actually exists? Is it a valid plugin JAR?");
        }
    }

}
