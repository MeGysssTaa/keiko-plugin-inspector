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

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.darksidecode.keiko.proxy.Keiko;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@UtilityClass
public class KeikoTools {

    private static final Collection<KeikoTool> tools = new ArrayList<>();

    static {
        tools.add(new Clean());
    }

    public static int executeToolWithArgs(@NonNull String toolName, @NonNull String[] toolArgs) {
        Optional<KeikoTool> tool = tools.stream()
                .filter(t -> t.getName().equalsIgnoreCase(toolName))
                .findAny();

        if (tool.isPresent())
            return tool.get().executeWithArgs(toolArgs);
        else {
            Keiko.INSTANCE.getLogger().warningLocalized("tool.notFound");
            return 1;
        }
    }

}
