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

        if (GlobalConfig.getLocale() != null
                && !GlobalConfig.getLocale().equalsIgnoreCase("system")) {
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
                // Cannot use Keiko logger yet (not initialized).
                //noinspection UseOfSystemOutOrSystemErr
                System.err.println("Skipped invalid message: bad localization format " +
                        "(key: \"" + key + "\", lang: \"" + lang.getLocale() + "\"): " + ex);
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
