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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class KeikoTools {

    public static void main(String[] unused) {
        List<Command> commands = Arrays.asList(
                new ChecksumCommand(),
                new ClearCachesCommand(),
                new ExitCommand(),
                new QInfoCommand(),
                new QRestoreCommand()
        );

        System.out.println("----------------------------------------------------------------------------");
        System.out.println("                          WELCOME [keiko-tools]");
        System.out.println("  This utility allows you to manage certain parts of Keiko even");
        System.out.println("  if your Minecraft server is power-off. Type \"?\" or \"help\" for help.");
        System.out.println("----------------------------------------------------------------------------");

        System.out.println("Available commands:");

        for (Command cmd : commands)
            System.out.printf("    %s - %s\n", cmd.getName(), cmd.getDescription());
        System.out.println("\n\n");

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
                    System.err.println("Unknown command. Available:");

                    for (Command cmd : commands)
                        System.out.printf("    %s - %s\n", cmd.getName(), cmd.getDescription());
                    System.out.println("\n\n");
                } else if (args.length < command.getMinArgsLen())
                    command.printUsage();
                else
                    command.executeSafely(args);
            }

            System.out.println("Exiting KeikoTools. Bye! (end of input)");
        } catch (IOException ex) {
            System.err.println("Failed to read input. Error:");
            ex.printStackTrace();
        }
    }

}
