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

package me.darksidecode.keiko.staticanalysis.cache;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.staticanalysis.StaticAnalysisResult;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class InspectionCache implements Serializable {

    public static final InspectionCache DEFAULT_EMPTY_CACHE
            = new InspectionCache(
                    System.currentTimeMillis(),
                    Keiko.INSTANCE.getBuildProperties().getVersion(),
                    Collections.emptyMap()
            );

    private static final long serialVersionUID = 8682874134440822991L;

    @Getter
    private final long creationDate;

    @NonNull @Getter
    private final String keikoVersion;

    // String (key) is analysis name
    @NonNull
    private final Map<String, StaticAnalysisResult> analysesResults;

    public Map<String, StaticAnalysisResult> getAnalysesResults() {
        return Collections.unmodifiableMap(analysesResults);
    }

    public String toJson() {
        return CommonJson.toJson(this);
    }

    public static InspectionCache fromJson(@NonNull String json) {
        return CommonJson.fromJson(json, InspectionCache.class);
    }

}
