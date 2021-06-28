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
