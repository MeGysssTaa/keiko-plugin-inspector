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

package me.darksidecode.keiko.tools;

import me.darksidecode.keiko.quarantine.Quarantine;
import me.darksidecode.keiko.quarantine.QuarantineEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class QInfoCommand extends Command {

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm");

    QInfoCommand() {
        super("qinfo", "Print information about a quarantined file.",
                "qinfo <original file path>", 1);
    }

    @Override
    protected void execute(String[] args) throws Exception {
        String origFn = args[0].trim();

        try {
            QuarantineEntry info = Quarantine.info(origFn);

            System.out.println("Quarantined since: "
                    + dateFormat.format(new Date(info.getQuarantinedDateTime())));
            System.out.println("Analysis name: " + info.getAnalysisName());
            System.out.println("Analysis result: " + info.getAnalysisResult());
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
            System.err.println("Are you sure file " + origFn + " has really been quarantined?");
        }
    }

}
