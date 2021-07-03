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

package me.darksidecode.keiko.time;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

public class BlockingClock extends SimpleClock {

    private final Object lock = new Object();

    @Override
    public Clock reset() {
        synchronized (lock) {
            return super.reset();
        }
    }

    @Override
    public long getElapsedMillis() {
        synchronized (lock) {
            return super.getElapsedMillis();
        }
    }

    @Override
    public boolean hasElapsed(long millis) {
        synchronized (lock) {
            return super.hasElapsed(millis);
        }
    }

    @Override
    public boolean hasElapsed(long time, @NonNull TimeUnit unit) {
        synchronized (lock) {
            return super.hasElapsed(time, unit);
        }
    }

}
