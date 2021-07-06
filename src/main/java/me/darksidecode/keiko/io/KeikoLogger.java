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

package me.darksidecode.keiko.io;

import com.diogonunes.jcolor.AnsiFormat;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.config.GlobalConfig;
import me.darksidecode.keiko.i18n.I18n;
import me.darksidecode.keiko.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.diogonunes.jcolor.Attribute.*;

@SuppressWarnings ("UseOfSystemOutOrSystemErr")
@RequiredArgsConstructor
public class KeikoLogger implements Prompter, Closeable {

    private static final String PREFIX = "[Keiko] ";

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"  );

    public static final AnsiFormat DEFAULT_FMT    = new AnsiFormat();
    public static final AnsiFormat BLACK_ON_WHITE = new AnsiFormat(BLACK_TEXT(), WHITE_BACK());
    public static final AnsiFormat BRIGHT_WHITE   = new AnsiFormat(BRIGHT_WHITE_TEXT());
    public static final AnsiFormat YELLOW         = new AnsiFormat(BRIGHT_YELLOW_TEXT());
    public static final AnsiFormat RED            = new AnsiFormat(BRIGHT_RED_TEXT());
    public static final AnsiFormat GREEN          = new AnsiFormat(BRIGHT_GREEN_TEXT());
    public static final AnsiFormat CYAN           = new AnsiFormat(BRIGHT_CYAN_TEXT());

    private final Object writeLock = new Object();

    @NonNull
    private final File logsDir;

    private FileWriter logWriter;

    private String lastLogDate;

    @Override
    public void prompt(@NonNull String message) {
        log(Level.USER_INPUT_REQUEST, BLACK_ON_WHITE, message);
    }

    @Override
    public void close() throws IOException {
        logWriter.close();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void log(@NonNull Level level, @NonNull String s, Object... format) {
        log(level, level.defaultAnsiFmt, s, format);
    }

    public void log(@NonNull Level level, @NonNull AnsiFormat ansiFmt,
                    @NonNull String s, Object... format) {
        print(level, System.out, ansiFmt, s, format);
    }

    public void logLocalized(@NonNull Level level, @NonNull String key, Object... args) {
        log(level, I18n.get(key, args));
    }

    public void logLocalized(@NonNull Level level, @NonNull AnsiFormat ansiFmt,
                             @NonNull String key, Object... args) {
        log(level, ansiFmt, I18n.get(key, args));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void debug(@NonNull String s, Object... format) {
        log(Level.DEBUG, s, format);
    }

    public void debug(@NonNull AnsiFormat ansiFmt, @NonNull String s, Object... format) {
        log(Level.DEBUG, ansiFmt, s, format);
    }

    public void debugLocalized(@NonNull String key, Object... args) {
        debug(I18n.get(key, args));
    }

    public void debugLocalized(@NonNull AnsiFormat ansiFmt, @NonNull String key, Object... args) {
        debug(ansiFmt, I18n.get(key, args));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void info(@NonNull String s, Object... format) {
        log(Level.INFO, s, format);
    }

    public void info(@NonNull AnsiFormat ansiFmt, @NonNull String s, Object... format) {
        log(Level.INFO, ansiFmt, s, format);
    }

    public void infoLocalized(@NonNull String key, Object... args) {
        info(I18n.get(key, args));
    }

    public void infoLocalized(@NonNull AnsiFormat ansiFmt, @NonNull String key, Object... args) {
        info(ansiFmt, I18n.get(key, args));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void warning(@NonNull String s, Object... format) {
        log(Level.WARNING, s, format);
    }

    public void warning(@NonNull AnsiFormat ansiFmt, @NonNull String s, Object... format) {
        log(Level.WARNING, ansiFmt, s, format);
    }

    public void warningLocalized(@NonNull String key, Object... args) {
        warning(I18n.get(key, args));
    }

    public void warningLocalized(@NonNull AnsiFormat ansiFmt, @NonNull String key, Object... args) {
        warning(ansiFmt, I18n.get(key, args));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void error(@NonNull String s, Object... format) {
        error(Level.ERROR.defaultAnsiFmt, s, format);
    }

    public void error(@NonNull AnsiFormat ansiFmt, @NonNull String s, Object... format) {
        Throwable t = null;
        Object lastFmt;

        if (format != null && format.length > 0
                && (lastFmt = format[format.length - 1]) instanceof Throwable) {
            t = (Throwable) lastFmt;
            format = format.length == 1 ? null
                    : Arrays.copyOf(format, format.length - 1);
        }

        log(Level.ERROR, ansiFmt, s, format);

        if (t != null) {
            error("    " + t);

            for (StackTraceElement line : t.getStackTrace())
                error("        at " + line);

            if (t.getCause() != null)
                error("    Caused by:", t.getCause());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void print(Level level, PrintStream printStream,
                       AnsiFormat ansiFmt, String message, Object... format) {
        if (level == Level.OFF)
            return; // don't log anything

        if (format != null && format.length > 0)
            message = String.format(message, format);

        synchronized (writeLock) {
            String currentDate = LocalDate.now().format(dateFormatter);
            String currentTime = LocalTime.now().format(timeFormatter);

            if (level.hasMinimum(GlobalConfig.getLogLevelConsole()))
                // Print colorized.
                printStream.println(ansiFmt.format(PREFIX + level.localizedPrefix + message));

            if (level.hasMinimum(GlobalConfig.getLogLevelFile()))
                printToFile(PREFIX + "[" + currentDate + "] " +
                        "[" + currentTime + "] " + level.localizedPrefix + message,
                        currentDate);
        }
    }

    private void printToFile(String message, String currentDate) {
        try {
            if (lastLogDate == null) {
                // This is the first entry to log.
                logWriter = new FileWriter(getLogFile(currentDate), true);
                lastLogDate = currentDate;
                deleteOldLogs();
            } else if (!currentDate.equals(lastLogDate)) {
                // Day changed, and there was no server restart.
                close();
                logWriter = new FileWriter(getLogFile(currentDate), true);
                lastLogDate = currentDate;
            }

            logWriter.append(message).append('\n').flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteOldLogs() {
        if (!logsDir.isDirectory())
            return;

        File[] logs = logsDir.listFiles();

        if (logs != null) {
            if (GlobalConfig.getLogsLifespanDays() != -1) {
                try {
                    long currentTime = System.currentTimeMillis();

                    for (File log : logs) {
                        // We could also just transform the file name (yyyy-MM-dd), but in that case
                        // we wouldn't respect the TIME this log file was created or last modified.
                        // File attributes allow us to keep "yesterday's" logs when date changes.
                        BasicFileAttributes attr = Files.readAttributes(log.toPath(), BasicFileAttributes.class);
                        long lastModifiedMillis = attr.lastModifiedTime().toMillis();

                        if (lastModifiedMillis == Long.MIN_VALUE || lastModifiedMillis == Long.MAX_VALUE)
                            warningLocalized("logsCleaner.dateErr", log.getName());
                        else {
                            long millisSinceLastModified = currentTime - lastModifiedMillis;
                            long daysSinceLastModified = TimeUnit.MILLISECONDS.toDays(millisSinceLastModified);

                            if (daysSinceLastModified > GlobalConfig.getLogsLifespanDays()) {
                                if (log.delete())
                                    debugLocalized("logsCleaner.deleteSuccess", log.getName());
                                else
                                    warningLocalized("logsCleaner.deleteErr", log.getName());
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException("failed to delete old logs", ex);
                }
            }
        }
    }

    private File getLogFile(String date) {
        if (!logsDir.exists())
            if (!logsDir.mkdirs())
                throw new IllegalStateException("failed to create logsDir: " + logsDir.getAbsolutePath());
        else if (logsDir.isFile())
            throw new IllegalStateException("logsDir is a file: " + logsDir.getAbsolutePath());

        File logFile = new File(logsDir, date + ".log");

        if (logFile.isDirectory())
            throw new IllegalStateException("logFile is a directory: " + logFile.getAbsolutePath());
        else {
            try {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
                return logFile;
            } catch (IOException ex) {
                throw new IllegalStateException("failed to create logFile: " + logFile.getAbsolutePath());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequiredArgsConstructor
    public enum Level {
        DEBUG              (DEFAULT_FMT   ),
        INFO               (BRIGHT_WHITE  ),
        WARNING            (YELLOW        ),
        ERROR              (RED           ),
        USER_INPUT_REQUEST (BLACK_ON_WHITE),
        OFF                (DEFAULT_FMT   ); // format is unused, but needed because of @NonNull's
        
        private final AnsiFormat defaultAnsiFmt;

        private String localizedPrefix; // null for Level.OFF

        private static final int EXTRA_PADDING = 2;

        private boolean hasMinimum(@NonNull Level minimum) {
            return ordinal() >= minimum.ordinal();
        }

        public static void initLocalizedLevelNames() { // cannot use <clinit> because its used early in GlobalConfig
            Level[] levels = values();
            String[] prefixes = new String[levels.length];
            int longestLen = 0;

            // Use localized prefixes for better accessibility.
            // * End before prefixes.length because Level.OFF does not need a prefix.
            for (int i = 0; i < prefixes.length - 1; i++) {
                String prefix = I18n.get("logLevel." + levels[i].name().toLowerCase());
                int prefixLen = prefix.length();

                if (prefixLen > longestLen)
                    longestLen = prefixLen;

                prefixes[i] = prefix;
            }

            // Pad to the length of the longest prefix.
            // * End before prefixes.length because Level.OFF does not need a prefix.
            for (int i = 0; i < prefixes.length - 1; i++)
                levels[i].localizedPrefix = StringUtils
                        .pad(prefixes[i], ' ', longestLen + EXTRA_PADDING);
        }
    }

}
