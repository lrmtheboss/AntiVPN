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

package dev.brighten.antivpn.database.sql.version;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.DatabaseException;
import dev.brighten.antivpn.database.VPNDatabase;
import dev.brighten.antivpn.database.local.version.First;
import dev.brighten.antivpn.database.sql.utils.Query;

import java.sql.SQLException;

public class MySQLFirst extends First {

    @Override
    public void update(VPNDatabase database) throws DatabaseException {
        try {
            Query.prepare("select `DATA_TYPE` from INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE table_name = 'responses' AND COLUMN_NAME = 'isp';").execute(set -> {
                if(set.getObject("DATA_TYPE").toString().contains("varchar")) {
                    AntiVPN.getInstance().getExecutor().log("Using old database format for storing responses! " +
                            "Dropping table and creating a new one...");
                    if(Query.prepare("drop table `responses`").execute() > 0) {
                        AntiVPN.getInstance().getExecutor().log("Successfully dropped table!");
                    }
                }
            });
        } catch (SQLException e) {
            throw new DatabaseException("Could not update MySQL database", e);
        }
        super.update(database);
    }
}
