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

public class SimpleClock implements Clock {

    private long clockTime;

    public SimpleClock() {
        this(System.currentTimeMillis());
    }

    public SimpleClock(long clockTime) {
        if (clockTime < 0L)
            throw new IllegalArgumentException("clockTime cannot be negative");

        this.clockTime = clockTime;
    }

    @Override
    public Clock reset() {
        clockTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public long getElapsedMillis() {
        return System.currentTimeMillis() - clockTime;
    }

    @Override
    public boolean hasElapsed(long millis) {
        return getElapsedMillis() >= millis;
    }

    @Override
    public boolean hasElapsed(long time, @NonNull TimeUnit unit) {
        return hasElapsed(unit.toMillis(time));
    }

}
