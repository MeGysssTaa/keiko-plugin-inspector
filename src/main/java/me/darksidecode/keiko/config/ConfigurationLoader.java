/*
 * Copyright 2019 DarksideCode
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

import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.util.ConfigUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ConfigurationLoader {

    private ConfigurationLoader() {}

    public static void load(Class configClass) {
        String configName = configClass.getSimpleName().
                replace("Config", "").toLowerCase();
        YamlConfiguration yaml = ConfigUtils.loadConfig(configName);

        try {
            Field yamlField = configClass.getDeclaredField("yaml");
            yamlField.setAccessible(true);

            if (Modifier.isStatic(yamlField.getModifiers())) {
                if (yamlField.getType() == YamlConfiguration.class)
                    yamlField.set(null /* static */, yaml);
                else
                    throw new RuntimeException("illegal yaml field in " + configClass.getName()
                            + ": must be of type " + YamlConfiguration.class.getName());
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
                String path = configAnno.path();

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

                Object val = yaml.get(path);

                if (val == null) {
                    // This field is not present in the configuration file.
                    KeikoPluginInspector.warn("Missing field %s in %s " +
                            "configuration. Falling back to default value!", path, configName);

                    // Assume that there is a default value available in the config class.
                    continue;
                }

                try {
                    if (Enum.class.isAssignableFrom(field.getType())) {
                        // This field is an Enum constant.
                        if (val instanceof String)
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

        KeikoPluginInspector.debug("Loaded %s configuration", configName);
    }

}
