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

package me.darksidecode.keiko.installation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class VersionTest {

    @ParameterizedTest
    @ValueSource (strings = {
            "1.0.0",
            "314124.581723.19895",
            "9.9.9-dev",
            "1.22.333-dEV",
            "0.0.25"
    })
    public void valueOf_DoesNotThrowMalformedVersionException(String originString) throws MalformedVersionException {
        Version.valueOf(originString);
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "hello",
            "v1.0.0",
            "10000000000000000.1.1",
            ".",
            "..",
            "...",
            "-",
            "0..1",
            ".1.0",
            "1.0.0-hello",
            "1.0.0-dev-dev",
            "1.0.0.0",
            "1.0.-"
    })
    public void valueOf_ThrowsMalformedVersionException(String originString) {
        MalformedVersionException ex = assertThrows(
                MalformedVersionException.class,
                () -> Version.valueOf(originString),
                "Expected a MalformedVersionException to be thrown"
        );

        assertNotNull(ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "1.0.0",
            "1.0.0-release",
            "1.0.1",
            "1.0.1-dev",
            "1.1.0",
            "1.1.1",
            "2.0.0",
            "2.0.1",
            "2.1.0",
            "2.1.1"
    })
    public void compareTo_GreaterThan_1_0_0_dev(String originString) throws MalformedVersionException {
        Version base = Version.valueOf("1.0.0-dev");
        Version version = Version.valueOf(originString);
        assertEquals(1, version.compareTo(base));
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "1.0.0",
            "1.0.0-release",
            "1.0.1",
            "1.0.1-dev",
            "1.1.0",
            "1.1.1",
            "2.0.0",
            "2.0.1",
            "2.1.0",
            "2.1.1",
            "2.2.0-dev"
    })
    public void compareTo_LessThan_2_2_0(String originString) throws MalformedVersionException {
        Version base = Version.valueOf("2.2.0");
        Version version = Version.valueOf(originString);
        assertEquals(-1, version.compareTo(base));
    }

    @ParameterizedTest
    @ValueSource (strings = {
            "01.0000.00000",
            "1.0.0-release",
            "1.0.0-rElEaSe"
    })
    public void compareTo_Equal_1_0_0(String originString) throws MalformedVersionException {
        Version base = Version.valueOf("1.0.0");
        Version version = Version.valueOf(originString);
        assertEquals(0, version.compareTo(base));
        assertEquals(base.toString(), version.toString());
        assertEquals(base.toCanonicalString(), version.toCanonicalString());
    }

}
