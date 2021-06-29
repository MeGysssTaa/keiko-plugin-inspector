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

package me.darksidecode.keiko.staticanalysis.cache;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.staticanalysis.StaticAnalysisResult;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class InspectionCache implements Serializable {

    private static final long serialVersionUID = 8682874134440822991L;

    @Getter
    private final long creationDate;

    @NonNull
    private final String keikoVersion;

    // String [key] is analysis (inspection) name.
    @NonNull
    private final Map<String, List<StaticAnalysisResult>> analysesResults;

    public static InspectionCache createEmptyCache() {
        return new InspectionCache(
                System.currentTimeMillis(),
                Keiko.INSTANCE.getBuildProperties().getVersion().toString(),
                new HashMap<>()
        );
    }

}
