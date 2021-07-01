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
import me.darksidecode.keiko.config.YamlHandle;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.registry.IdentityFilter;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ConfigurationUtils {

    public static List<IdentityFilter> getExclusionsList(@NonNull YamlHandle conf, @NonNull String path) {
        List<IdentityFilter> filters = new ArrayList<>();

        try {
            List<String> matchers = conf.get(path);

            if (matchers == null)
                return filters;

            for (String matcher : matchers) {
                IdentityFilter filter = new IdentityFilter(matcher);

                if (filter.getErrorI18nKey() == null)
                    filters.add(filter); // add valid filter
                else {
                    Keiko.INSTANCE.getLogger().warningLocalized(
                            IdentityFilter.ERR_PREFIX + "skippingInvalidExclusion");
                    Keiko.INSTANCE.getLogger().warningLocalized(filter.getErrorI18nKey());
                    Keiko.INSTANCE.getLogger().warning("    - \"%s\"", matcher);
                }
            }
        } catch (Exception ex) {
            Keiko.INSTANCE.getLogger().warningLocalized(
                    IdentityFilter.ERR_PREFIX + "skippingInvalidExclusion");
            Keiko.INSTANCE.getLogger().error(
                    "Unhandled exception (totally invalid configuration?)", ex);
        }

        return filters;
    }

}
