/*
 * Copyright 2020 DarksideCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
