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
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.staticanalysis.StaticAnalysis;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@RequiredArgsConstructor
public class InspectionCache implements Serializable {

    private static final long serialVersionUID = 8682874134440822991L;

    private final long creationDate;

    private final String keikoVersion;

    private final String fileHash;

    // String - analysis name
    private final Map<String, StaticAnalysis.Result> analysesResults;

    boolean hasExpired(long lifespanDays, String installedKeikoVersion) {
        return System.currentTimeMillis() > (creationDate + TimeUnit.DAYS.toMillis(lifespanDays))
                || !installedKeikoVersion.equals(keikoVersion);
    }

}
