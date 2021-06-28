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

package me.darksidecode.keiko.runtimeprotect;

import lombok.Getter;
import me.darksidecode.keiko.config.RuntimeProtectConfig;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.runtimeprotect.dac.KeikoSecurityManager;

public class RuntimeProtect {

    @Getter
    private boolean enabled;

    public void setupDomainAccessControl() {
        if (!RuntimeProtectConfig.getDomainAccessControlEnabled())
            return;

        SecurityManager mgr = new KeikoSecurityManager();
        System.setSecurityManager(mgr);
        enabled = true; // indicate that Keiko DAC was successfully enabled
        Keiko.INSTANCE.getLogger().debugLocalized("runtimeProtect.dac.enabled");
    }

}
