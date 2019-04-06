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

package me.darksidecode.keiko.tools;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor (access = AccessLevel.PACKAGE)
abstract class Command {

    @NonNull @Getter (AccessLevel.PACKAGE)
    private final String name, description, usage;

    @Getter (AccessLevel.PACKAGE)
    private final int minArgsLen;

    void executeSafely(String[] args) {
        try {
            execute(args);
        } catch (Throwable t) {
            System.err.println("Failed to process your command. Error:");
            t.printStackTrace();
        }
    }

    protected abstract void execute(String[] args) throws Exception;

    void printUsage() {
        System.err.println("Use: " + usage);
    }

}
