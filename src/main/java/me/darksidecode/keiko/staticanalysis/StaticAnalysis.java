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

import lombok.Getter;
import me.darksidecode.kantanj.system.Shell;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.util.References;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class StaticAnalysis implements Opcodes {

    /**
     * Constants used to make the code in analysis classes less cumbersome.
     */
    protected static final String PROCESS_BUILDER_NAME = References.transformedClassName(ProcessBuilder.class);
    protected static final String OFFLINE_PLAYER_NAME  = References.transformedClassName(OfflinePlayer.class);
    protected static final String COMMAND_SENDER_NAME  = References.transformedClassName(CommandSender.class);
    protected static final String HUMAN_ENTITY_NAME    = References.transformedClassName(HumanEntity.class);
    protected static final String RUNTIME_NAME         = References.transformedClassName(Runtime.class);
    protected static final String SYSTEM_NAME          = References.transformedClassName(System.class);
    protected static final String PLAYER_NAME          = References.transformedClassName(Player.class);
    protected static final String BUKKIT_NAME          = References.transformedClassName(Bukkit.class);
    protected static final String SERVER_NAME          = References.transformedClassName(Server.class);
    protected static final String SHELL_NAME           = References.transformedClassName(Shell.class);




    @Getter
    private final String name;
    private final String inputJarName;

    protected final Collection<ClassNode> classes;

    @Getter
    protected Result result;

    public StaticAnalysis(String name, String inputJarName, Collection<ClassNode> classes) {
        this.name = name;
        this.inputJarName = inputJarName;
        this.classes = classes;
    }

    final Result run() {
        KeikoPluginInspector.debug("Beginning static analysis %s on JAR %s", name, inputJarName);

        try {
            KeikoPluginInspector.debug("│        ├── other");

            if ((result = analyzeOther()) != null)
                return result;

            int clsIdx = -1, lastClsIdx = classes.size() - 1;
            KeikoPluginInspector.debug("│        ├── classes");

            for (ClassNode clsNode : classes) {
                clsIdx++;
                String clsTreeSymbol = (clsIdx == lastClsIdx) ? "└──" : "├──";

                KeikoPluginInspector.debug("│        │        " +
                        "%s class %s", clsTreeSymbol, clsNode.name);

                if ((result = analyzeClass(clsNode)) != null)
                    return result;

                if (clsNode.fields != null) {
                    int fldIdx = -1, lastFldIdx = clsNode.fields.size() - 1;
                    KeikoPluginInspector.debug("│        │        │        " +
                            "├── fields");

                    for (FieldNode fldNode : clsNode.fields) {
                        fldIdx++;

                        String fldTreeSymbol = (fldIdx == lastFldIdx) ? "└──" : "├──";
                        @SuppressWarnings ("Duplicates")
                        String offsetStr = (fldIdx == lastFldIdx)
                                ? "│        │        │        └─────── "
                                ////////////////////////////////////////
                                : "│        │        │        │        ";

                        KeikoPluginInspector.debug(offsetStr +
                                        "%s %s : %s", fldTreeSymbol, fldNode.name, fldNode.desc);

                        if ((result = analyzeField(clsNode, fldNode)) != null)
                            return result;
                    }
                }

                if (clsNode.methods != null) {
                    int mtdIdx = -1, lastMtdIdx = clsNode.methods.size() - 1;
                    KeikoPluginInspector.debug("│        │        │        " +
                            "├── methods");

                    for (MethodNode mtdNode : clsNode.methods) {
                        mtdIdx++;

                        String mtdTreeSymbol = (mtdIdx == lastMtdIdx) ? "└──" : "├──";
                        String offsetStr = (mtdIdx == lastMtdIdx)
                                ? "│        │        │        └─────── "
                                ////////////////////////////////////////
                                : "│        │        │        │        ";

                        KeikoPluginInspector.debug(offsetStr +
                                        "%s %s : %s", mtdTreeSymbol, mtdNode.name, mtdNode.desc);

                        if ((result = analyzeMethod(clsNode, mtdNode)) != null)
                            return result;
                    }
                }
            }
        } catch (Throwable t) {
            KeikoPluginInspector.warn("Failed to run static analysis " + name + " on file " + inputJarName);
            KeikoPluginInspector.warn("Cause:");

            t.printStackTrace();

            if (GlobalConfig.getFailurePolicy() == FailurePolicy.SHUTDOWN) {
                KeikoPluginInspector.warn("Shutting down as per configured failure policy.");
                Bukkit.shutdown();
            }
        }

        KeikoPluginInspector.debug("Finished static analysis %s on JAR %s", name, inputJarName);

        return Result.DEFAULT;
    }

    protected Result analyzeOther (                                     ) throws Exception { return null; }
    protected Result analyzeClass (ClassNode clsNode                    ) throws Exception { return null; }
    protected Result analyzeField (ClassNode clsNode, FieldNode  fldNode) throws Exception { return null; }
    protected Result analyzeMethod(ClassNode clsNode, MethodNode mtdNode) throws Exception { return null; }

    @Getter
    public static class Result implements Serializable {
        private static final long serialVersionUID = 6984235770180089898L;

        private static final Result DEFAULT = new Result(
                Type.ALL_CLEAN, 0.0, Collections.singletonList("<default>"));

        private final Type type;

        private final double confidencePercent;

        private final List<String> details;

        private final Countermeasures recommendedCountermeasures;

        public Result(Type type, double confidencePercent, List<String> details) {
            this(type, confidencePercent, details, null);
        }

        public Result(Type type, double confidencePercent,
               List<String> details, Countermeasures recommendedCountermeasures) {
            this.type = type;
            this.confidencePercent = confidencePercent;
            this.details = details;
            this.recommendedCountermeasures = recommendedCountermeasures;
        }

        @Override
        public String toString() {
            return String.format(
                    "StaticAnalysis.Result.%s[%s%%](%s)->%s",
                    type, confidencePercent, details, recommendedCountermeasures
            );
        }

        public enum Type {
            /**
             * Keiko is certain that the analyzed subject is fully clean.
             */
            ALL_CLEAN,

            /**
             * Keiko has some suspicions regarding the analyzed subject.
             */
            SUSPICIOUS,

            /**
             * Keiko is certain that the analyzed subject is malicious (unwanted malware).
             */
            MALICIOUS
        }
    }

}
