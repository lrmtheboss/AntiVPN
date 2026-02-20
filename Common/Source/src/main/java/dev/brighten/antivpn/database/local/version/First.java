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

package dev.brighten.antivpn.database.local.version;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.DatabaseException;
import dev.brighten.antivpn.database.VPNDatabase;
import dev.brighten.antivpn.database.sql.utils.ExecutableStatement;
import dev.brighten.antivpn.database.sql.utils.Query;
import dev.brighten.antivpn.database.version.Version;
import dev.brighten.antivpn.utils.MiscUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class First implements Version<VPNDatabase> {

    private final List<AutoCloseable> toClose = new ArrayList<>();
    @Override
    public void update(VPNDatabase database) throws DatabaseException {
        try {
            closeOnEnd(Query.prepare("create table if not exists `whitelisted` (`uuid` varchar(36) not null)"))
                    .execute();
            closeOnEnd(Query.prepare("create table if not exists `whitelisted-ips` (`ip` varchar(45) not null)"))
                    .execute();
            closeOnEnd(Query
                    .prepare("create table if not exists `responses` (`ip` varchar(45) not null, `asn` varchar(12),"
                    + "`countryName` text, `countryCode` varchar(10), `city` text, `timeZone` varchar(64), "
                    + "`method` varchar(32), `isp` text, `proxy` boolean, `cached` boolean, `inserted` timestamp,"
                    + "`latitude` double, `longitude` double)")).execute();
            closeOnEnd(Query.prepare("create table if not exists `alerts` (`uuid` varchar(36) not null)"))
                    .execute();
            closeOnEnd(Query.prepare("create table if not exists `database_version` (`version` int)")).execute();
            closeOnEnd(Query.prepare("insert into `database_version` (`version`) values (?)")
                    .append(versionNumber())).execute();

            AntiVPN.getInstance().getExecutor().log("Creating indexes...");
            closeOnEnd(Query.prepare("create index if not exists `uuid_1` on `whitelisted` (`uuid`)")).execute();
            closeOnEnd(Query.prepare("create index if not exists `ip_1` on `responses` (`ip`)")).execute();
            closeOnEnd(Query.prepare("create index if not exists `proxy_1` on `responses` (`proxy`)")).execute();
            closeOnEnd(Query.prepare("create index if not exists `inserted_1` on `responses` (`inserted`)")).execute();
            closeOnEnd(Query.prepare("create index if not exists `ip_1` on `whitelisted-ips` (`ip`)")).execute();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update database", e);
        } finally {
            MiscUtils.close(toClose.toArray(AutoCloseable[]::new));
            toClose.clear();
        }
    }

    private ExecutableStatement closeOnEnd(ExecutableStatement statement) {
        toClose.add(statement);
        return statement;
    }

    @Override
    public int versionNumber() {
        return 0;
    }

    @Override
    public boolean needsUpdate(VPNDatabase database) {
        try(var statement = Query.prepare("select * from `database_version` where version = 0")) {
            try(ResultSet set =  statement.executeQuery()) {
                return set.getFetchSize() == 0;
            }
        } catch (SQLException e) {
            return true;
        }

    }
}
