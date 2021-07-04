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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class EventBus {

    private final Map<Class<? extends Event>, Collection<Listener>> listeners = new HashMap<>();

    /**
     * Dispatch all events in a separate thread. The rationale behind this is:
     *
     *     1. We want our checks to run in background, without interrupting or pausing any
     *        actions. We don't want the server to lag if one of our heuristics lags.
     *
     *     2. This allows us to react to events seamlessly from another thread. For example,
     *        we cannot de-op a player right inside the OpUpdate event listener because that
     *        task involves writing to a file, and since the event is called before the initial
     *        (original) write to the ops file, our de-opping file write will be overwritten, or,
     *        in other words, will not work. But from another thread, we can easily do so.
     *
     * We don't really want multiple threads to be used for event dispatch, though, or at least
     * that's my thoughts at the moment. Events are better synchronized and ordered as they were.
     */
    private final Executor executor = Executors.newSingleThreadExecutor();

    public Collection<Listener> getListenersOf(Class<? extends Event> eventType) {
        return listeners.computeIfAbsent(eventType, k -> new ArrayList<>());
    }

    public void dispatchEvent(@NonNull Event event) {
        executor.execute(() -> {
            Collection<Listener> listeners = getListenersOf(event.getClass());

            for (Listener listener : listeners) {
                try {
                    event.dispatch(listener);
                } catch (Exception ex) {
                    Keiko.INSTANCE.getLogger().error("Unhandled exception in a Megane event listener", ex);
                }
            }
        });
    }

}
