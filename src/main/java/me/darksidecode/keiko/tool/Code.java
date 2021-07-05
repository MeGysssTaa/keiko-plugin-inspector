/*
 * Copyright (C) 2019-2021 German Vekhorev (DarksideCode)
 *
 * This file is part of Keiko Plugin Inspector.
 *
 * Keiko Plugin Inspector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Keiko Plugin Inspector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Keiko Plugin Inspector.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.darksidecode.keiko.tool;

import me.darksidecode.jminima.disassembling.SimpleJavaDisassembler;
import me.darksidecode.jminima.phase.PhaseExecutionException;
import me.darksidecode.jminima.phase.PhaseExecutionWatcher;
import me.darksidecode.jminima.phase.basic.CloseJarFilePhase;
import me.darksidecode.jminima.phase.basic.DisassemblePhase;
import me.darksidecode.jminima.phase.basic.OpenJarFilePhase;
import me.darksidecode.jminima.phase.basic.PrintClassBytecodePhase;
import me.darksidecode.jminima.printing.SimpleBytecodePrinter;
import me.darksidecode.jminima.workflow.Workflow;
import me.darksidecode.jminima.workflow.WorkflowExecutionResult;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.util.Holder;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Disassembles the given JAR-file (generates '.txt' files with JVM bytecode mnemonics and class data).
 */
class Code extends KeikoTool {

    Code() {
        super(1);
    }

    @Override
    protected int execute(String[] args) throws Exception {
        String inputFilePath = String.join(" ", args); // allow spaces in file paths
        File inputFile = new File(inputFilePath);

        if (!inputFile.isFile()) {
            Keiko.INSTANCE.getLogger().warningLocalized(
                    getI18nPrefix() + "notFile", inputFile.getAbsolutePath());
            return 1;
        }

        File outputDir = new File(Keiko.INSTANCE.getEnv().getWorkDir(),
                "disassembled-output/" + inputFile.getName().replace(".jar", ""));

        if (outputDir.exists()) {
            Keiko.INSTANCE.getLogger().warningLocalized(
                    getI18nPrefix() + "outExists", outputDir.getAbsolutePath());
            return 1;
        }

        if (!outputDir.mkdirs()) {
            Keiko.INSTANCE.getLogger().warningLocalized(
                    getI18nPrefix() + "outMkFail", outputDir.getAbsolutePath());
            return 1;
        }

        return disassemble(inputFile, outputDir);
    }

    private int disassemble(File inputFile, File outputDir) {
        Keiko.INSTANCE.getLogger().infoLocalized(getI18nPrefix() + "wait");
        Holder<Map<? extends ClassNode, String>> outputHolder = new Holder<>();

        try (Workflow workflow = new Workflow()
                .phase(new OpenJarFilePhase(inputFile))
                .phase(new DisassemblePhase(SimpleJavaDisassembler.class))
                .phase(new CloseJarFilePhase())
                .phase(new PrintClassBytecodePhase(SimpleBytecodePrinter.class)
                        .watcher(new PhaseExecutionWatcher<Map<? extends ClassNode, String>>()
                                .doAfterExecution((val, err) -> outputHolder.setValue(val))))) {
            WorkflowExecutionResult result = workflow.executeAll();

            if (result != WorkflowExecutionResult.FULL_SUCCESS) {
                String errType = result == WorkflowExecutionResult.FATAL_FAILURE ? "errFatal" : "err";
                Keiko.INSTANCE.getLogger().warningLocalized(getI18nPrefix() + errType);

                for (PhaseExecutionException err : workflow.getAllErrorsChronological())
                    Keiko.INSTANCE.getLogger().error("State: disassemble", err);

                if (result == WorkflowExecutionResult.FATAL_FAILURE)
                    return 1;
            }

            Map<? extends ClassNode, String> output = outputHolder.getValue(); // 100% non-null for non-fatal

            for (ClassNode cls : output.keySet()) {
                String clsCode = output.get(cls);
                File outputFile = new File(outputDir, cls.name + ".txt");
                File parent = outputFile.getParentFile();

                if (!parent.exists() && !parent.mkdirs())
                    // Just warn, don't exit at this point.
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            getI18nPrefix() + "outMkFail", parent.getAbsolutePath());

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
                    writer.write(clsCode);
                } catch (IOException ex) {
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            getI18nPrefix() + "saveErr", outputFile.getAbsolutePath(), ex.toString());
                }
            }

            Keiko.INSTANCE.getLogger().infoLocalized(
                    getI18nPrefix() + "complete", outputDir.getAbsolutePath());
        }

        return 0;
    }

}
