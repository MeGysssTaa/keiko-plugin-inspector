/*
 * Copyright 2021 German Vekhorev (DarksideCode)
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

package me.darksidecode.keiko.io;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class UserInputRequest<T> {

    public static final int INFINITE_ATTEMPTS = -1;

    private final InputStream inputStream;

    private final Converter<T> cvt;

    private volatile Prompter prompter;

    private volatile String prompt;

    private volatile int maxAttempts = INFINITE_ATTEMPTS;

    private volatile boolean trim = true;

    private volatile int attemptsMade = 1;

    private volatile boolean used;

    public synchronized int getAttemptsLeft() {
        expectState(true);
        return maxAttempts - attemptsMade;
    }

    public synchronized boolean hasMoreAttempts() {
        expectState(true);
        return maxAttempts == INFINITE_ATTEMPTS || attemptsMade < maxAttempts;
    }

    public synchronized int getAttemptsMade() {
        expectState(true);
        return attemptsMade;
    }

    public synchronized T block() {
        expectState(false);
        used = true;
        T input = null;

        while (hasMoreAttempts() && (input = tryRead()) == null)
            attemptsMade++;

        return input;
    }

    private synchronized void expectState(boolean expectUsed) {
        if (used && !expectUsed)
            throw new IllegalStateException("cannot be reused");
        else if (!used && expectUsed)
            throw new IllegalStateException("not used yet");
    }

    private synchronized T tryRead() {
        prompter.prompt(prompt);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine();

            if (line == null)
                throw new IllegalStateException("unexpected end of input stream"); // fatal

            return tryConvert(line);
        } catch (IOException ex) {
            throw new RuntimeException("failed to read the specified input stream", ex); // fatal
        }
    }

    private synchronized T tryConvert(String line) {
        if (trim) line = line.trim();
        if (line.isEmpty()) return null;
        return cvt.convert(line);
    }

    public static <T> Builder<T> newBuilder(InputStream inputStream, Class<? extends T> acceptedDataType) {
        return new Builder<>(inputStream, acceptedDataType);
    }

    public static final class Builder<T> {
        private final UserInputRequest<T> result;

        private Builder(@NonNull InputStream inputStream, @NonNull Class<? extends T> acceptedDataType) {
            Converter<T> cvt = new Converter<>(acceptedDataType);
            result = new UserInputRequest<>(inputStream, cvt);
        }

        public Builder<T> prompt(@NonNull Prompter prompter, @NonNull String prompt) {
            result.prompter = prompter;
            result.prompt = prompt;

            return this;
        }

        public Builder<T> maxAttempts(int maxAttempts) {
            if (maxAttempts < 1)
                throw new IllegalArgumentException("maxAttempts cannot be smaller than 1");

            result.maxAttempts = maxAttempts;
            return this;
        }

        public Builder<T> trim(boolean trim) {
            result.trim = trim;
            return this;
        }

        public UserInputRequest<T> build() {
            return result;
        }
    }

}
