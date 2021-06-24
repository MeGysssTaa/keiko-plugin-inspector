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

package me.darksidecode.keiko.registry;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class IdentityFilterTest {

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   Filter identity type: FILE
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ValueSource (strings = {
            "FILE=/home/server/test.jar"
    })
    public void matches_AllFalse_FILE(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertFalse(filter.matches(new Identity(
                "/home/server/MyPlugin.jar", "",
                "", "")));
        assertFalse(filter.matches(new Identity(
                "/", "",
                "", "")));
        assertFalse(filter.matches(new Identity(
                "*", "",
                "", "m")));
        assertFalse(filter.matches(new Identity(
                "", "",
                "", "")));
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "FILE=/home/server/test.jar",
            "FILE=/home/server/?*.jar",
            "FILE=*"
    })
    public void matches_AllTrue_FILE(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertTrue(filter.matches(new Identity(
                "/home/server/test.jar", "",
                "", "")));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   Filter identity type: PLUGIN
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ValueSource (strings = {
            "PLUGIN=MyPlugin"
    })
    public void matches_AllFalse_PLUGIN(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertFalse(filter.matches(new Identity(
                "", "AnotherPlugin",
                "", "")));
        assertFalse(filter.matches(new Identity(
                "", "*",
                "", "")));
        assertFalse(filter.matches(new Identity(
                "", "",
                "", "methodName")));
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "PLUGIN=MyPlugin",
            "PLUGIN=My*",
            "PLUGIN=??Plugin"
    })
    public void matches_AllTrue_PLUGIN(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertTrue(filter.matches(new Identity(
                "", "MyPlugin",
                "", "")));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   Filter identity type: SOURCE
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ValueSource (strings = {
            "SOURCE=my.fq.ClassName"
    })
    public void matches_AllFalse_SOURCEwithoutMethod(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertFalse(filter.matches(new Identity(
                "", "",
                "f.q.ClassName", "")));
        assertFalse(filter.matches(new Identity(
                "", "",
                "*", "")));
        assertFalse(filter.matches(new Identity(
                "", "",
                "", "")));

        assertFalse(filter.matches(new Identity(
                "", "",
                "f.q.ClassName", "methodName")));
        assertFalse(filter.matches(new Identity(
                "", "",
                "*", "*")));
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "SOURCE=my.fq.ClassName",
            "SOURCE=my.fq.*",
            "SOURCE=my.fq.ClassName#*",
    })
    public void matches_AllTrue_SOURCEwithoutMethod(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertTrue(filter.matches(new Identity(
                "", "",
                "my.fq.ClassName", "")));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   Filter identity type: SOURCE
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @ParameterizedTest
    @ValueSource (strings = {
            "SOURCE=my.fq.ClassName#goodMethod"
    })
    public void matches_AllFalse_SOURCEwithMethod(String input) {
        matches_AllFalse_SOURCEwithoutMethod(input); // same code, different value source
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "SOURCE=my.fq.ClassName#goodMethod",
            "SOURCE=my.fq.ClassName#good?*d",
            "SOURCE=my.fq.*#goodMethod",
            "SOURCE=my.fq.*#*",
    })
    public void matches_AllTrue_SOURCEwithMethod(String input) {
        IdentityFilter filter = new IdentityFilter(input);

        assertTrue(filter.matches(new Identity(
                "", "",
                "my.fq.ClassName", "goodMethod")));
    }

}
