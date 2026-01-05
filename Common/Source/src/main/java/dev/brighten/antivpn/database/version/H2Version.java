/*
 * Copyright 2026 Dawson Hessler
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

package dev.brighten.antivpn.database.version;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.local.H2VPN;
import dev.brighten.antivpn.database.local.version.First;
import dev.brighten.antivpn.database.local.version.Second;
import dev.brighten.antivpn.database.sql.utils.MySQL;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface H2Version extends Version<H2VPN> {

    H2Version[] versions = new H2Version[] {new First(), new Second()};

    default void backupDatabase() {
        File dataFolder = new File(AntiVPN.getInstance().getPluginFolder(), "databases");

        if(!dataFolder.exists()) {
            return;
        }

        List<File> files = new ArrayList<>(List.of(Optional.ofNullable(dataFolder.listFiles()).orElse(new File[0])));
        files.sort(Comparator.comparingLong(File::lastModified));

        for (File file : files) {
            MySQL.backupOldDB(file, dataFolder);
        }
    }
}
