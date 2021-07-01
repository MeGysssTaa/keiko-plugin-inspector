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

package me.darksidecode.keiko.runtimeprotect.megane.heur;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.darksidecode.keiko.i18n.I18n;
import me.darksidecode.keiko.proxy.Keiko;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class Report {

    @Getter
    private final Severity severity;

    private final List<String> lines;

    public void print() {
        for (String line : lines)
            Keiko.INSTANCE.getLogger().warning(line);
    }

    public static Builder newBuilder(@NonNull Severity severity) {
        return new Builder(severity);
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }

    public static final class Builder {
        private final Severity severity;
        private final List<String> lines = new ArrayList<>();

        private Builder(Severity severity) {
            this.severity = severity;
            addOutline();
        }

        private void addOutline() {
            lines.add(" ");
            lines.add("=====================================================================================");
            lines.add(" ");
        }

        public Builder line(@NonNull String s) {
            lines.add(s);
            return this;
        }

        public Builder localizedLine(@NonNull String i18nKey, Object... args) {
            lines.add(I18n.get(i18nKey, args));
            return this;
        }

        public Report build() {
            addOutline();
            return new Report(severity, lines);
        }
    }

}
