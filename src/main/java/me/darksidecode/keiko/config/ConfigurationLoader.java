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

package me.darksidecode.keiko.config;

import lombok.NonNull;
import me.darksidecode.keiko.installer.KeikoInstaller;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ConfigurationLoader {

    private ConfigurationLoader() {}

    public static void load(@NonNull KeikoInstaller installer, @NonNull Class<?> configClass) {
        String configName = configClass.getSimpleName().
                replace("Config", "").toLowerCase();
        YamlHandle handle = loadConfig(installer, configName);

        try {
            Field handleField = configClass.getDeclaredField("handle");
            handleField.setAccessible(true);

            if (Modifier.isStatic(handleField.getModifiers())) {
                if (handleField.getType() == YamlHandle.class)
                    handleField.set(null /* static */, handle);
                else
                    throw new RuntimeException("illegal yaml field in " + configClass.getName()
                            + ": must be of type " + YamlHandle.class.getName());
            } else
                throw new RuntimeException("illegal yaml field " +
                        "in " + configClass.getName() + ": must be static");
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}

        for (Field field : configClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Config.class)) {
                if (!(Modifier.isStatic(field.getModifiers())))
                    throw new RuntimeException("illegal field " + field.getName() + " in config "
                            + configClass.getName() + ": all configuration fields must be static");

                field.setAccessible(true);

                Config configAnno = field.getAnnotation(Config.class);
                String path = configAnno.value();

                if (path.isEmpty()) {
                    // Default (infer by field name, e.g. 'fieldName' => 'field_name'').
                    char[] chars = field.getName().toCharArray();
                    StringBuilder pathBuilder = new StringBuilder();

                    for (char ch : chars) {
                        if (Character.isLowerCase(ch))
                            pathBuilder.append(ch);
                        else
                            pathBuilder.append('_').append(Character.toLowerCase(ch));
                    }

                    path = pathBuilder.toString();
                }

                Object val = handle.get(path);

                if (val == null)
                    // This field is not present in the configuration file.
                    // Assume that there is a default value available in the config class.
                    continue;

                try {
                    if (Enum.class.isAssignableFrom(field.getType())) {
                        // This field is an Enum constant.
                        if (val instanceof String)
                            //noinspection rawtypes
                            val = Enum.valueOf((Class<Enum>) field.getType(), ((String) val).toUpperCase());
                        else
                            throw new RuntimeException("field " + field.getName() + " in " + configClass.getName()
                                    + " is of type Enum: " + field.getType().getName() + ", value at "
                                    + path + " must be a String");
                    }

                    field.set(null /* static */, field.getType().cast(val));
                } catch (Exception ex) {
                    throw new RuntimeException("failed to inject configuration field " + field.getName()
                            + " of " + configClass.getName() + "; value at path " + path + ": " + val, ex);
                }

                try {
                    Field validatorField = configClass.getDeclaredField(field.getName() + "Validator");

                    if (validatorField.getType().equals(Runnable.class)) {
                        validatorField.setAccessible(true);

                        try {
                            Runnable validator = (Runnable) validatorField.get(null /* static */);
                            validator.run();
                        } catch (Exception ex) {
                            throw new RuntimeException("failed to validate configuration " +
                                    "field " + field.getName() + " in " + configClass.getName(), ex);
                        }
                    } else
                        throw new RuntimeException("illegal validator field for " + field.getName()
                                + " in " + configClass.getName() + ": must be of type " + Runnable.class.getName());
                } catch (NoSuchFieldException ignored) { /* no validation desired for this field */ }
            }
        }
    }

    private static YamlHandle loadConfig(KeikoInstaller installer, String configName) {
        Yaml yaml = new Yaml();
        File configsFolder = new File(installer.getWorkDir(), "config/");
        //noinspection ResultOfMethodCallIgnored
        configsFolder.mkdirs();

        File configFile = new File(configsFolder, configName + ".yml");

        if (configFile.exists()) {
            try (Reader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(configFile), StandardCharsets.UTF_8))) {
                return new YamlHandle(yaml.load(reader));
            } catch (Exception ex) {
                File moveTo = new File(configFile.getAbsolutePath() + "-INVALID.backup~");

                try {
                    Files.move(configFile.toPath(), moveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException moveEx) {
                    moveEx.printStackTrace();
                }

                throw new RuntimeException("invalid " + configName + " configuration", ex);
            }
        } else {
            // Install and retry.
            installer.checkInstallation("config/" + configName + ".yml");
            return loadConfig(installer, configName);
        }
    }

}
