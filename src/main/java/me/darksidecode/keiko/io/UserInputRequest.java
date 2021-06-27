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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class UserInputRequest<T> {

    public static final int INFINITE_ATTEMPTS = -1;

    private final InputStream inputStream;

    private final Converter<T> cvt;

    private volatile Prompter prompter;

    private volatile String prompt;

    private volatile int maxAttempts = INFINITE_ATTEMPTS;

    private volatile boolean closeAfterRead;

    private final List<Function<String, String>> lineTransformers = new ArrayList<>();

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

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            while (hasMoreAttempts() && (input = tryRead(reader)) == null)
                attemptsMade++;

            return input;
        } catch (IOException ex) {
            throw new RuntimeException("fatal i/o exception", ex);
        } finally {
            if (reader != null && closeAfterRead) {
                try {
                    reader.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private synchronized void expectState(boolean expectUsed) {
        if (used && !expectUsed)
            throw new IllegalStateException("cannot be reused");
        else if (!used && expectUsed)
            throw new IllegalStateException("not used yet");
    }

    private synchronized T tryRead(BufferedReader reader) throws IOException {
        prompter.prompt(prompt);

        String line = reader.readLine();

        if (line == null)
            throw new EOFException("unexpected end of user input stream"); // fatal

        return tryConvert(line);
    }

    private synchronized T tryConvert(String line) {
        for (Function<String, String> transformer : lineTransformers)
            line = transformer.apply(line);

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

        public Builder<T> closeAfterRead(boolean closeAfterRead) {
            result.closeAfterRead = closeAfterRead;
            return this;
        }

        public Builder<T> lineTransformer(@NonNull Function<String, String> transformer) {
            result.lineTransformers.add(transformer);
            return this;
        }

        public Builder<T> lineTransformers(@NonNull List<Function<String, String>> transformers) {
            result.lineTransformers.addAll(transformers);
            return this;
        }

        public UserInputRequest<T> build() {
            return result;
        }
    }

}
