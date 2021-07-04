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

package me.darksidecode.keiko.reflect.minecraft;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.Keiko;
import me.darksidecode.keiko.reflect.PublicFieldExtractor;
import me.darksidecode.keiko.reflect.ReflectCached;
import me.darksidecode.keiko.reflect.WrappedObject;

import java.lang.reflect.Field;

public class WrappedServerCommand extends WrappedObject {

    private static final Class<?>
            serverCommandClass;

    private static final Field
            commandField;

    static {
        try {
            String className = Keiko.INSTANCE.getLoader()
                    .getInjector().getCollector().getPlaceholderApplicator()
                    .apply("net.minecraft.server.{nms_version}.ServerCommand");

            serverCommandClass = Keiko.INSTANCE.getLoader().getLoadedClass(className);
            commandField = serverCommandClass.getDeclaredField("command");
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private final ReflectCached<String> command;

    public WrappedServerCommand(@NonNull Object handle) {
        super(serverCommandClass, handle);
        command = new ReflectCached<>(new PublicFieldExtractor<>(commandField, handle));
    }

    public String getCommand() {
        return command.get();
    }

}
