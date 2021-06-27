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

package me.darksidecode.keiko.util;

import lombok.experimental.UtilityClass;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.system.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class JsonFileUtils {

    public static <T> T readCompressedJsonUtf8(File file, Class<T> type) {
        byte[] bytes = FileUtils.readGZIP(file);
        String json = new String(bytes, StandardCharsets.UTF_8);
        return CommonJson.fromJson(json, type);
    }

    public static void writeCompressedJsonUtf8(File file, Object obj, FileUtils.OverwriteMode mode) {
        String json = CommonJson.toJson(obj);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        FileUtils.writeGZIP(file, bytes, mode);
    }

}
