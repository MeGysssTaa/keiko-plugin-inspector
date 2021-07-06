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

import me.darksidecode.keiko.io.KeikoLogger;
import me.darksidecode.keiko.proxy.Keiko;

import java.io.File;

/**
 * Cleans Keiko's ".artifacts/". directory.
 */
class Clean extends KeikoTool {

    @Override
    protected int execute(String[] args) throws Exception {
        File artifactsDir = new File(Keiko.INSTANCE.getEnv().getWorkDir(), ".artifacts/");
        Result result = new Result();

        if (artifactsDir.isDirectory())
            deleteRecursively(artifactsDir, result);

        Keiko.INSTANCE.getLogger().infoLocalized(
                KeikoLogger.GREEN, getI18nPrefix() + "success",
                result.filesDeleted, result.bytesDeleted, result.errors);

        return 0;
    }

    private void deleteRecursively(File file, Result result) {
        try {
            if (file.isDirectory()) {
                File[] children = file.listFiles();

                if (children != null)
                    for (File child : children)
                        deleteRecursively(child, result);
            }

            deleteFile(file, result);
        } catch (Exception ex) {
            result.errors++;
        }
    }

    private void deleteFile(File file, Result result) {
        long bytes = file.isFile() ? file.length() : 0; // undefined for directories

        if (file.delete()) {
            result.filesDeleted++;
            result.bytesDeleted += bytes;
        } else
            result.errors++;
    }

    private static class Result {
        private int filesDeleted, bytesDeleted, errors;
    }

}
