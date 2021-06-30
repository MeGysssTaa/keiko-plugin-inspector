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

package me.darksidecode.keiko.proxy.injector.injection.bungee;

import me.darksidecode.keiko.proxy.injector.Inject;

/**
 * BungeeCord does not respect parent class loaders at all by default.
 * This causes errors (namely, "NoClassDefFoundError" for any Bungee plugins
 * API class, or when loading transitive dependencies from other plugins).
 * This injection fixes its LibraryLoader to respect KeikoClassLoader.
 *
 * @see PluginClassloaderInjection
 */
@Inject (
        inClass = "net.md_5.bungee.api.plugin.LibraryLoader",
        inMethod = "createLoader(" +
                "Lnet/md_5/bungee/api/plugin/PluginDescription;" +
                ")Ljava/lang/ClassLoader;"
)
public class LibraryClassloaderInjection extends PluginClassloaderInjection {

    public LibraryClassloaderInjection(String inClass, String inMethodName, String inMethodDesc) {
        super(inClass, inMethodName, inMethodDesc);
    }

}
