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

package me.darksidecode.keiko.util;

public final class ArrayUtils {

    private ArrayUtils() {}

    public static void mergeArrays(byte[] dest, byte[]... arrays) {
        if (dest == null)
            throw new NullPointerException("destination array cannot be null");

        if ((arrays == null) || (arrays.length == 0))
            throw new IllegalArgumentException("arrays list cannot be null or empty");

        int totalLength = 0;

        for (byte[] array : arrays)
            totalLength += array.length;

        if (dest.length != totalLength)
            throw new IllegalArgumentException(
                    "destination array length is " + dest.length + ", expected to be " + totalLength);

        int offset = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, dest, offset, array.length);
            offset += array.length;
        }
    }

}
