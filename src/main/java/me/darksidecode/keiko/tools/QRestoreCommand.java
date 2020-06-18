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

import me.darksidecode.keiko.KeikoPluginInspector;
import me.darksidecode.keiko.quarantine.Quarantine;

class QRestoreCommand extends Command {

    QRestoreCommand() {
        super("qrestore", "Restore a file from quarantine.",
                "qrestore <original file path>", 1);
    }

    @Override
    protected void execute(String[] args) throws Exception {
        String origFn = args[0].trim();

        try {
            Quarantine.restore(origFn);
            KeikoPluginInspector.info("Successfully restored the specified file from quarantine");
        } catch (Exception ex) {
            KeikoPluginInspector.warn("Error: %s", ex.getMessage());
            KeikoPluginInspector.warn("Are you sure file %s has really been quarantined?", origFn);
        }
    }

}
