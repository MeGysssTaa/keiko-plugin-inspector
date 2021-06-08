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
//package me.darksidecode.keiko.quarantine;
//
//import me.darksidecode.kantanj.formatting.CommonJson;
//import me.darksidecode.keiko.KeikoPluginInspector;
//import me.darksidecode.keiko.OLD_staticanalysis.StaticAnalysis;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.StandardCopyOption;
//import java.util.NoSuchElementException;
//
//public final class Quarantine {
//
//    private Quarantine() {}
//
//    private static final String QUARANTINED_SUFFIX = ".quarantined~";
//    private static final String QUARANTINE_INFO_SUFFIX = ".qinfo";
//
//    public static void settle(StaticAnalysis analysis,
//                              StaticAnalysis.Result analysisResult, File infectedFile) {
//        try {
//            File folder = getFolder();
//            File dest = new File(folder, infectedFile.getName() + QUARANTINED_SUFFIX);
//            File qInfoFile = new File(dest.getAbsolutePath() + QUARANTINE_INFO_SUFFIX);
//
//            Files.move(infectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//
//            byte[] qInfo = CommonJson.toJson(new QuarantineEntry(
//                    infectedFile.getAbsolutePath(),
//                    analysis.getName(),
//                    analysisResult,
//                    System.currentTimeMillis())
//            ).getBytes(StandardCharsets.UTF_8);
//
//            Files.write(qInfoFile.toPath(), qInfo);
//        } catch (IOException ex) {
//            throw new RuntimeException("failed to " +
//                    "settle file " + infectedFile.getName() + " in quarantine");
//        }
//    }
//
//    public static QuarantineEntry info(String origFileName) {
//        String quarantinedName = origFileName + QUARANTINED_SUFFIX;
//
//        File folder = getFolder();
//        File quarantinedFile = new File(folder, quarantinedName);
//
//        if (quarantinedFile.exists()) {
//            File qInfoFile = new File(quarantinedFile.getAbsolutePath() + QUARANTINE_INFO_SUFFIX);
//
//            if (qInfoFile.exists()) {
//                try {
//                    String qInfo = new String(Files.readAllBytes(
//                            qInfoFile.toPath()), StandardCharsets.UTF_8);
//                    return CommonJson.fromJson(qInfo, QuarantineEntry.class);
//                } catch (IOException ex) {
//                    throw new RuntimeException("failed to read " +
//                            "quarantine info file " + qInfoFile.getAbsolutePath(), ex);
//                }
//            } else
//                throw new RuntimeException("missing quarantine info file for file "
//                        + origFileName + " (searched by: '" + qInfoFile.getName() + "')");
//        } else
//            throw new NoSuchElementException("no such file in Keiko quarantine:" +
//                    " " + origFileName + " (searched by: '" + quarantinedName + "')");
//    }
//
//    public static QuarantineEntry restore(String origFileName) {
//        QuarantineEntry qInfo = info(origFileName); // also ensures the file is valid and quarantined
//        File origFile = new File(qInfo.getOriginalFilePath());
//
//        if (origFile.exists())
//            throw new IllegalStateException(
//                    "original file location is already taken: " + origFile.getAbsolutePath());
//
//        String quarantinedName = origFileName + QUARANTINED_SUFFIX;
//
//        File folder = getFolder();
//        File quarantinedFile = new File(folder, quarantinedName);
//
//        try {
//            Files.move(quarantinedFile.toPath(), origFile.toPath());
//            File qInfoFile = new File(quarantinedFile.getAbsolutePath() + QUARANTINE_INFO_SUFFIX);
//
//            //noinspection ResultOfMethodCallIgnored
//            qInfoFile.delete();
//
//            return qInfo;
//        } catch (IOException ex) {
//            throw new RuntimeException("failed to move "
//                    + quarantinedFile.getAbsolutePath()
//                    + " to " + origFile.getAbsolutePath(), ex);
//        }
//    }
//
//    private static File getFolder() {
//        File folder = new File(KeikoPluginInspector.getWorkDir(), ".quarantine/");
//
//        if (!(folder.exists()))
//            //noinspection ResultOfMethodCallIgnored
//            folder.mkdirs();
//
//        return folder;
//    }
//
//}
