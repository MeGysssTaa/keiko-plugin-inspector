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

package me.darksidecode.keiko.installation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import me.darksidecode.kantanj.networking.GetHttpRequest;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.keiko.proxy.Keiko;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.TimerTask;

@RequiredArgsConstructor
public class KeikoUpdater extends TimerTask {

    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String LATEST_RELEASE = "repos/MeGysssTaa/keiko-plugin-inspector/releases/latest";

    private final String installedVersion;

    private final boolean download;

    @Override
    public void run() {
        Keiko.INSTANCE.getLogger().debugLocalized("updater.checking");
        String jsonString = getReleasesJson();

        if (jsonString == null)
            // Error.
            return;

        JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonArray assets = json.getAsJsonArray("assets");
        JsonObject release = assets.get(0).getAsJsonObject();

        String latestVersion = json.get("name").getAsString();
        String downloadUrl = release.get("browser_download_url").getAsString();

        if (latestVersion.startsWith("v"))
            latestVersion = latestVersion.substring(1); // omit the "v" (version) prefix

        if (!latestVersion.equals(installedVersion)) {
            // We found an update!
            Keiko.INSTANCE.getLogger().warningLocalized("updater.updFound", latestVersion);

            if (download) {
                Keiko.INSTANCE.getLogger().warningLocalized("updater.downloading", latestVersion);
                downloadAndInstall(downloadUrl);
            }
        }
    }

    private String getReleasesJson() {
        try {
            GetHttpRequest request = new GetHttpRequest()
                    .baseUrl(GITHUB_API_URL)
                    .path(LATEST_RELEASE)
                    .userAgent("keiko-plugin-inspector/v" + installedVersion)
                    .requestProperty("Accept", "application/vnd.github.v3+json")
                    .connectTimeout(5000)
                    .readTimeout(15000)
                    .asGetRequest();

            return Networking.Http.get(request);
        } catch (Exception ex) {
            Keiko.INSTANCE.getLogger().warningLocalized("updater.checkErr");
            Keiko.INSTANCE.getLogger().error("Failed to check for updates", ex);

            // Error.
            return null;
        }
    }

    private void downloadAndInstall(String jarUrl) {
        File keikoExecutable = Keiko.INSTANCE.getKeikoExecutable();

        if (!keikoExecutable.delete()) {
            Keiko.INSTANCE.getLogger().warningLocalized("updater.errDownload");
            Keiko.INSTANCE.getLogger().error("Failed to delete the old Keiko executable");

            return;
        }

        try {
            URL url = new URL(jarUrl);
            FileUtils.copyURLToFile(url, keikoExecutable, 5000, 60000);

            Keiko.INSTANCE.getLogger().warning(" ");
            Keiko.INSTANCE.getLogger().warning("=================================================================");
            Keiko.INSTANCE.getLogger().warningLocalized("updater.installedLine1");
            Keiko.INSTANCE.getLogger().warningLocalized("updater.installedLine2");
            Keiko.INSTANCE.getLogger().warning("=================================================================");
            Keiko.INSTANCE.getLogger().warning(" ");

            if (Keiko.INSTANCE.getLaunchState() == Keiko.LaunchState.LAUNCHING) {
                // This is the initial (not periodical) updates check. Abort startup and
                // tell the user to re-run Keiko, but this time the latest version of it.
                Keiko.INSTANCE.getLogger().warningLocalized("updater.aborting");
                System.exit(0);
            }
        } catch (IOException ex) {
            Keiko.INSTANCE.getLogger().warningLocalized("updater.errDownload");
            Keiko.INSTANCE.getLogger().error("Failed to copy URL to file", ex);
        }
    }

}
