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
import me.darksidecode.keiko.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class KeikoTools {

    public static void main(String[] unused) {
        KeikoPluginInspector.earlyBoot(Platform.STANDALONE);
        KeikoPluginInspector.info(" ");

        List<Command> commands = Arrays.asList(
                new ChecksumCommand(),
                new ClearCachesCommand(),
                new ExitCommand(),
                new InspectCommand(),
                new QInfoCommand(),
                new QRestoreCommand()
        );

        KeikoPluginInspector.info("----------------------------------------------------------------------------");
        KeikoPluginInspector.info("                          WELCOME [keiko-tools]");
        KeikoPluginInspector.info("  This utility allows you to manage certain parts of Keiko even");
        KeikoPluginInspector.info("  if your Minecraft server is power-off. Type \"?\" or \"help\" for help.");
        KeikoPluginInspector.info("----------------------------------------------------------------------------");
        KeikoPluginInspector.info(" ");
        KeikoPluginInspector.info("Available commands:");

        for (Command cmd : commands)
            KeikoPluginInspector.info("    %s - %s", cmd.getName(), cmd.getDescription());

        KeikoPluginInspector.info(" ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty())
                    continue;

                String label = line.split(" ")[0];
                String[] args = line.replace(label + " ", "").split(" ");

                if ((args.length == 1) && (args[0].equals(line)))
                    // e.g. "command" → (len=1)["command"]
                    args = new String[0];

                Command command = commands.stream().filter(cmd -> cmd.getName().
                        equalsIgnoreCase(label)).findFirst().orElse(null);

                if (command == null) {
                    KeikoPluginInspector.warn("Unknown command. Available:");

                    for (Command cmd : commands)
                        KeikoPluginInspector.info("    %s - %s", cmd.getName(), cmd.getDescription());
                    KeikoPluginInspector.info(" ");
                } else if (args.length < command.getMinArgsLen())
                    command.printUsage();
                else
                    command.executeSafely(args);
            }

            KeikoPluginInspector.info("Exiting KeikoTools. Bye! (end of input)");
        } catch (IOException ex) {
            KeikoPluginInspector.warn("Failed to read input. Error:");
            ex.printStackTrace();
        }
    }

}
