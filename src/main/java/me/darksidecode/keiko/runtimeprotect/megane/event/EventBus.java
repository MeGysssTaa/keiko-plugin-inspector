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

package me.darksidecode.keiko.runtimeprotect.megane.event;

import lombok.NonNull;
import me.darksidecode.keiko.proxy.Keiko;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventBus {

    private final Map<Class<? extends Event>, Collection<Listener>> listeners = new HashMap<>();

    public EventBus() {
        wireEvents();
    }

    private void wireEvents() {
        Reflections reflections = new Reflections("me.darksidecode.keiko.runtimeprotect.megane");

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(WireEvents.class)) {
            if (!Listener.class.isAssignableFrom(clazz))
                throw new IllegalArgumentException(
                        "class " + clazz.getName() + " is annotated with @WireEvents, " +
                                "but does not inherit from " + Listener.class.getName());

            Class<? extends Listener> listenerClass = (Class<? extends Listener>) clazz;
            Listener listener;

            try {
                Constructor<? extends Listener> ctor = listenerClass.getConstructor();
                listener = ctor.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(
                        "failed to instantiate listener " + clazz.getName()
                                + ", does it have a public default (no-parameters) constructor?");
            }

            WireEvents anno = clazz.getAnnotation(WireEvents.class);
            Class<? extends Event>[] eventTypes = anno.value();

            for (Class<? extends Event> eventType : eventTypes)
                listenersOf(eventType).add(listener);
        }
    }

    private Collection<Listener> listenersOf(Class<? extends Event> eventType) {
        return listeners.computeIfAbsent(eventType, k -> new ArrayList<>());
    }

    public void dispatchEvent(@NonNull Event event) {
        Collection<Listener> listeners = listenersOf(event.getClass());

        for (Listener listener : listeners) {
            try {
                event.dispatch(listener);
            } catch (Exception ex) {
                Keiko.INSTANCE.getLogger().error("Unhandled exception in a Megane event listener", ex);
            }
        }
    }

}
