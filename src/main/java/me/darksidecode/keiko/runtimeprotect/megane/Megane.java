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

package me.darksidecode.keiko.runtimeprotect.megane;

import lombok.Getter;
import me.darksidecode.keiko.runtimeprotect.megane.event.Event;
import me.darksidecode.keiko.runtimeprotect.megane.event.EventBus;
import me.darksidecode.keiko.runtimeprotect.megane.heur.Heuristic;
import me.darksidecode.keiko.runtimeprotect.megane.heur.RegisterHeuristic;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;

public class Megane {

    @Getter
    private final EventBus eventBus = new EventBus();

    public Megane() {
        loadHeuristics();
    }

    private void loadHeuristics() {
        Reflections reflections = new Reflections("me.darksidecode.keiko.runtimeprotect.megane.heur.impl");

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(RegisterHeuristic.class)) {
            if (!Heuristic.class.isAssignableFrom(clazz))
                throw new IllegalArgumentException(
                        "class " + clazz.getName() + " is annotated with @RegisterHeuristic, " +
                                "but does not inherit from " + Heuristic.class.getName());

            Class<? extends Heuristic> listenerClass = (Class<? extends Heuristic>) clazz;
            Heuristic heur;

            try {
                Constructor<? extends Heuristic> ctor = listenerClass.getConstructor();
                heur = ctor.newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(
                        "failed to instantiate Heuristic " + clazz.getName()
                                + ", has it got a public default (no-parameters) constructor?", ex);
            }

            if (heur.isEnabled()) {
                // Only subscribe to events if this heuristic is enabled in config.
                RegisterHeuristic anno = clazz.getAnnotation(RegisterHeuristic.class);
                Class<? extends Event>[] eventTypes = anno.value();

                for (Class<? extends Event> eventType : eventTypes)
                    eventBus.getListenersOf(eventType).add(heur);
            }
        }
    }

}
