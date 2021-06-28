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

    @ParameterizedTest
    @ValueSource (strings = {
            "SOURCE=my.fq.ClassName#goodMethod"
    })
    public void matches_AllFalse_SOURCEwithMethod(String input) {
        matches_AllFalse_SOURCEwithoutMethod(input); // same code, different value source
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "SOURCE=my.fq.ClassName",
            "SOURCE=my.fq.*",
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
