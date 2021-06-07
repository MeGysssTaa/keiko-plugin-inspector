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

package me.darksidecode.keiko.i18n;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.proxy.Keiko;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class I18n {

    private static final Map<String, FormattedLocalizedMessageBuilder> map = new ConcurrentHashMap<>();

    private static final String INVALID_LOCALE =
            "invalid locale: " + GlobalConfig.getLocale() + " - must be in format language_COUNTRY " +
            "(case-sensetive; 'language' and 'COUNTRY' both consist of two Latin letters)";

    static {
        Locale locale;

        if (GlobalConfig.getLocale() != null) {
            String[] spl = GlobalConfig.getLocale().split("_");

            if (spl.length == 2) {
                String language = spl[0];
                String country  = spl[1];

                if (isValidLocale(language, false) && isValidLocale(country, true))
                    locale = new Locale(language, country);
                else
                    throw new IllegalArgumentException(INVALID_LOCALE);
            } else
                throw new IllegalArgumentException(INVALID_LOCALE);
        } else
            locale = Locale.getDefault();

        ResourceBundle lang = ResourceBundle.getBundle(
                "lang/keiko", locale, Utf8ResourceBundleControl.INSTANCE);

        for (String key : lang.keySet()) {
            try {
                map.put(key, new FormattedLocalizedMessageBuilder(lang.getString(key)));
            } catch (Exception ex) {
                Keiko.INSTANCE.getLogger().error(
                        "Skipped invalid message: bad localization format of %s in %s: %s",
                        key, lang.getLocale(), ex.getMessage());
            }
        }
    }

    private static boolean isValidLocale(String s, boolean upper) {
        if (s.length() != 2)
            return false;

        char[] chars = s.toCharArray();

        for (char c : chars)
            if (Character.isUpperCase(c) != upper)
                return false;

        return true;
    }

    public static String get(@NonNull String key, Object... args) {
        FormattedLocalizedMessageBuilder builder = map.get(key);

        if (builder != null)
            return builder.build(args);
        else {
            Keiko.INSTANCE.getLogger().error("Invalid localization key: %s", key);
            return key;
        }
    }

}
