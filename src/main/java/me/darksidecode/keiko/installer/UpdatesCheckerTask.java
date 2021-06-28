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

package me.darksidecode.keiko.installer;

import me.darksidecode.kantanj.networking.SampleUserAgents;
import me.darksidecode.keiko.KeikoPluginInspector;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdatesCheckerTask implements Runnable {

    private static final String SPIGOT_API_URL         = "https://api.spigotmc.org";
    private static final String SPIGOT_RESOURCES_URL   = "https://www.spigotmc.org/resources/";
    private static final String KEIKO_SPIGOT_PLUGIN_ID = "66278";

    @Override
    public void run() {
        try {
            // FIXME: Cannot use kantanj because of BungeeCord compatibility issues:
            //     java.lang.NoSuchMethodError: org.apache.commons.io.IOUtils.readLines(Ljava/io/InputStream;Ljava/nio/charset/Charset;)Ljava/util/List;
            //        at me.darksidecode.kantanj.networking.Networking$Http.readResponseAndDisconnect(Networking.java:130)
            //        at me.darksidecode.kantanj.networking.Networking$Http.get(Networking.java:84)
//            GetHttpRequest request = new GetHttpRequest().
//                    baseUrl(SPIGOT_API_URL).
//                    path("legacy/update.php?resource=" + KEIKO_SPIGOT_PLUGIN_ID).
//                    userAgent(SampleUserAgents.MOZILLA_WIN_NT).
//                    asGetRequest();

            URL url = new URL(SPIGOT_API_URL + "/legacy/update.php?resource=" + KEIKO_SPIGOT_PLUGIN_ID);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            con.setRequestProperty("User-Agent", SampleUserAgents.MOZILLA_WIN_NT);
            con.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String latestVersion = reader.readLine(); // Spigot should only return one line

            reader.close();
            con.disconnect();

            // (see above) String latestVersion = Networking.Http.get(request);
            String installedVersion = KeikoPluginInspector.getVersion();

            if (!(installedVersion.equals(latestVersion))) {
                // A newer version of Keiko was found on SpigotMC.
                KeikoPluginInspector.info(" ");
                KeikoPluginInspector.info(KeikoPluginInspector.LINE);
                KeikoPluginInspector.info("  (i) A new version of Keiko is available!");
                KeikoPluginInspector.info("      Installed: %s, latest: %s", installedVersion, latestVersion);
                KeikoPluginInspector.info("      Make sure to update as soon as possible:");
                KeikoPluginInspector.info("      %s", SPIGOT_RESOURCES_URL + KEIKO_SPIGOT_PLUGIN_ID);
                KeikoPluginInspector.info(KeikoPluginInspector.LINE);
                KeikoPluginInspector.info(" ");
            }
        } catch (Exception ex) {
            KeikoPluginInspector.warn(" ");
            KeikoPluginInspector.warn(KeikoPluginInspector.LINE);
            KeikoPluginInspector.warn("  (!) Failed to check for updates! Is everything OK with");
            KeikoPluginInspector.warn("      your Internet connection? P.S.: you can disable updates");
            KeikoPluginInspector.warn("      checking in .../Keiko/config/global.yml (not recommended).");
            KeikoPluginInspector.warn(KeikoPluginInspector.LINE);
            KeikoPluginInspector.warn(" ");

            ex.printStackTrace();
        }
    }

}
