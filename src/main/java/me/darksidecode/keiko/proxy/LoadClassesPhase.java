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

package me.darksidecode.keiko.proxy;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.jminima.JMinima;
import me.darksidecode.jminima.phase.EmittedValue;
import me.darksidecode.jminima.phase.Phase;
import me.darksidecode.jminima.phase.PhaseExecutionException;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor (access = AccessLevel.PACKAGE)
class LoadClassesPhase extends Phase<JarFile, LoadClassesPhase.Result> {

    private static final String DEFAULT_ERR_MSG_HEADER
            = "the following errors occurred during classes loading:";

    @NonNull
    private final KeikoClassLoader classLoader;

    @Override
    public Class<? super JarFile> getTargetTypeClass() {
        return JarFile.class;
    }

    @Override
    protected EmittedValue<? extends LoadClassesPhase.Result> execute(JarFile target,
                                                                      PhaseExecutionException error)
                                                                      throws Exception {
        if (target == null)
            return new EmittedValue<>(new PhaseExecutionException(
                    true, "failed to define classes from the given target data", error));

        StringBuilder errMsgBuilder = new StringBuilder(DEFAULT_ERR_MSG_HEADER);
        Result result = defineClasses(target.entries(), errMsgBuilder);
        String errMsg = errMsgBuilder.toString();

        if (errMsg.equals(DEFAULT_ERR_MSG_HEADER))
            return new EmittedValue<>(result); // full success
        else
            return new EmittedValue<>(result,
                   new PhaseExecutionException(false, errMsg)); // error(s)
    }

    private Result defineClasses(Enumeration<JarEntry> entries, StringBuilder errMsgBuilder) {
        Result result = new Result();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".class")) {
                try {
                    classLoader.findClass(entry.getName()
                            .replace('/', '.')
                            .replace(".class", "")
                    );

                    result.successes++;
                } catch (Throwable t) {
                    if (JMinima.debug) t.printStackTrace();
                    errMsgBuilder.append("\n    - ").append(t);
                    result.failures++;
                }
            }
        }

        return result;
    }

    static class Result {
        private Result() {}
        int successes, failures;
    }

}
