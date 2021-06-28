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

package me.darksidecode.keiko.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.system.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class JsonFileUtils {

    public static <T> T readCompressedJsonUtf8(@NonNull File file, @NonNull Class<T> type) {
        byte[] bytes = FileUtils.readGZIP(file);
        String json = new String(bytes, StandardCharsets.UTF_8);
        return CommonJson.fromJson(json, type);
    }

    public static void writeCompressedJsonUtf8(@NonNull File file, @NonNull Object obj,
                                               @NonNull FileUtils.OverwriteMode mode) {
        String json = CommonJson.toJson(obj);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        FileUtils.writeGZIP(file, bytes, mode);
    }

}
