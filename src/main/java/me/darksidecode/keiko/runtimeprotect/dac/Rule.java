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

package me.darksidecode.keiko.runtimeprotect.dac;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.registry.IndexedPlugin;
import me.darksidecode.keiko.runtimeprotect.CallerInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

class Rule {

    private static final Map<String, Supplier<String>> placeholders =
            ImmutableMap.<String, Supplier<String>>builder().
                    put("{plugins_folder}", KeikoPluginInspector.getPluginsFolder()::getAbsolutePath).
                    put("{server_folder}", KeikoPluginInspector.getServerFolder()::getAbsolutePath).
            build();

    @Getter (AccessLevel.PACKAGE)
    private final Type filterType;

    @Getter (AccessLevel.PACKAGE)
    private final IdentityFilter identityFilter;

    @Getter (AccessLevel.PACKAGE)
    private final Object filteredObject;

    @Getter (AccessLevel.PACKAGE)
    private final String arg;

    Rule(String s) {

        // TODO: 30.03.2019 complex checking for contradictory rules

        if (s.contains("\\")) {
            KeikoPluginInspector.warn("NOTE: Rule \"%s\" contains backslash characters ('\\'). " +
                    "Automatically replacing them with regular slashes ('/') (those also work fine on Windows!)");
            s = s.replace("\\", "/"); // Windows's directory separator + Regex != love
        }

        try {
            String[] tokens = s.split(" ");

            if (tokens.length < 3)
                throw new IllegalArgumentException("insufficient number of words/commands (tokens), required: 3+");

            String typeStr = tokens[0].toUpperCase().trim();

            try {
                filterType = Type.valueOf(typeStr);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("illegal rule type: " +
                        typeStr + ", expected one of: " + Arrays.toString(Type.values()));
            }

            String fullIdentityStr = tokens[1].trim();
            String[] identityArgs = fullIdentityStr.split("=");

            if (identityArgs.length > 2)
                throw new IllegalArgumentException("invalid number of arguments in indentity " +
                        "specification: \"" + fullIdentityStr + "\", expected either one or two");

            String identityFilterStr = identityArgs[0].toUpperCase().trim();

            try {
                identityFilter = IdentityFilter.valueOf(identityFilterStr);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("illegal identity filter type: " +
                        identityFilterStr + ", expected one of: " + Arrays.toString(IdentityFilter.values()));
            }

            IndexedPlugin filteredPlugin = null;

            if (identityFilter == IdentityFilter.PLUGIN) {
                String pluginName = identityArgs[1].trim();
                filteredPlugin = KeikoPluginInspector.getPluginContext().getPlugin(pluginName);

                if (filteredPlugin == null)
                    throw new NoSuchElementException("no such plugin installed: \"" + pluginName + "\"");

                filteredObject = filteredPlugin;
            } else if (identityFilter == IdentityFilter.SOURCE) {
                String source = identityArgs[1].trim();

                if (source.contains("#")) {
                    String[] sourceData = source.split("#");

                    if (sourceData.length != 2)
                        throw new IllegalArgumentException("invalid source data length: " +
                                "\"" + source + "\", expected exactly two arguments separated by #");

                    String className = sourceData[0];
                    String methodName = sourceData[1];

                    filteredObject = new FilteredSource(className, methodName);
                } else
                    filteredObject = new FilteredSource(source, null);
            } else
                filteredObject = null;

            // Omit first two elements (rule type and identity type).
            String[] args = new String[tokens.length - 2];
            System.arraycopy(tokens, 2, args, 0, args.length);

            String arg = String.join(" ", args); // support for args like "/directory/with spaces/meh"

            for (String placeholder : placeholders.keySet())
                arg = arg.replace(placeholder, placeholders.get(placeholder).get());

            if (filteredPlugin != null)
                arg = arg.
                        replace("{plugin_name}", filteredPlugin.getName()).
                        replace("{plugin_jar_path}", filteredPlugin.getJar().getAbsolutePath());

            this.arg = arg;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "invalid rule (please check your syntax, setup and plugins): \"" + s + "\"", ex);
        }
    }

    boolean filterCaller(CallerInfo callerInfo) {
        switch (identityFilter) {
            default: // ALL
                return true;

            case PLUGIN:
                return callerInfo.getPlugin() == filteredObject;

            case SOURCE:
                FilteredSource source = (FilteredSource) filteredObject;
                return callerInfo.getClazz().equals(source.getClassName())
                        && ((source.getMethodName() == null)
                            || (callerInfo.getMethod().equals(source.getMethodName())));
        }
    }

    enum Type {
        ALLOW,
        DENY
    }

    enum IdentityFilter {
        ALL,
        PLUGIN,
        SOURCE
    }

    @RequiredArgsConstructor (access = AccessLevel.PRIVATE)
    private static class FilteredSource {
        @Getter (AccessLevel.PACKAGE)
        private final String className, methodName;
    }

}
