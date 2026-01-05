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
import dev.brighten.antivpn.database.local.H2VPN;
import dev.brighten.antivpn.database.sql.utils.Query;
import dev.brighten.antivpn.database.version.Version;
import dev.brighten.antivpn.utils.CIDRUtils;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Second implements Version<VPNDatabase> {
    @Override
    public void update(VPNDatabase database) throws DatabaseException {
        if(database instanceof H2VPN h2VPN) {
            h2VPN.backupDatabase();
        }
        List<String> whitelistedIps = new ArrayList<>();

        try (var set = Query.prepare("SELECT * FROM `whitelisted-ips`").executeQuery()) {
            while (set.next()) {
                whitelistedIps.add(set.getString("ip"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not get whitelisted ips from database!", e);
        }

        try {
            Query.prepare("CREATE TABLE IF NOT EXISTS `whitelisted-ranges` " +
                            "(id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "cidr_string VARCHAR(45), " +
                            "ip_start BIGINT NOT NULL, " +
                            "ip_end BIGINT NOT NULL")
                    .execute();
            Query.prepare("CREATE INDEX idx_ip_range ON `whitelisted-ranges` (ip_start, ip_end)").execute();

            var cidrs = whitelistedIps.stream().map(ip -> {
                try {
                    return new CIDRUtils(ip + "/32");
                } catch (UnknownHostException e) {
                    throw new RuntimeException("Could not format ip " + ip + " into a CIDR!", e);
                }
            }).toList();
            var insertStatement = Query.prepare("INSERT INTO `whitelisted-ranges` (`cidr_string`, `ip_start`, `ip_end`) VALUES (?, ?, ?)");
            for (CIDRUtils cidr : cidrs) {
                insertStatement = insertStatement
                        .append(cidr.toString())
                        .append(cidr.getStartIpInt())
                        .append(cidr.getEndIpInt())
                        .addBatch();
            }

            int[] updateCounts = insertStatement.executeBatch();

            for (int updateCount : updateCounts) {
                if(updateCount == 0) {
                    throw new RuntimeException("Could not insert a CIDR from previous whitelisted lists, attempted to restore previous database!");
                }
            }

            Query.prepare("DROP INDEX ip_1 on `whitelisted-ips`").execute();
            Query.prepare("DROP TABLE `whitelisted-ips`").execute();
            Query.prepare("INSERT INTO `database_version` (`version`) VALUES (?)").append(versionNumber()).execute();
        } catch (Throwable e) {
            AntiVPN.getInstance().getExecutor().log("Failed to update database to version 1: " + e.getMessage());
            try {
                rollback(whitelistedIps);
            } catch (SQLException ex) {
                throw new DatabaseException("Failed to rollback database!", e);
            }
            throw new DatabaseException("Failed to update to version one, rolling back database!", e);
        }

    }

    private void rollback(List<String> ipAddresses) throws SQLException {
        AntiVPN.getInstance().getExecutor().log("Rolling back to version 0...");
        Query.prepare("DROP INDEX idx_ip_range ON `whitelisted-ranges`").execute();
        Query.prepare("DROP TABLE `whitelisted-ranges`").execute();
        Query.prepare("DELETE FROM `database_version` WHERE version = ?").append(versionNumber()).execute();

        Query.prepare("CREATE TABLE IF NOT EXISTS `whitelisted-ips` (`ip` VARCHAR(45) NOT NULL)")
                .execute();
        Query.prepare("create index if not exists `ip_1` on `whitelisted-ips` (`ip`)").execute();

        Query.prepare("DELETE FROM `whitelisted-ips`").execute();

        var statement = Query.prepare("INSERT INTO `whitelisted-ips` (`ip`) VALUES (?)");
        for (String ip : ipAddresses) {
            statement.append(ip);
            statement.addBatch();
        }

        statement.executeBatch();
    }

    @Override
    public int versionNumber() {
        return 1;
    }

    @Override
    public boolean needsUpdate(VPNDatabase database) {
        try (ResultSet set = Query.prepare("select * from `database_version` where version = 1").executeQuery()) {
            return set.getFetchSize() == 0;
        } catch (SQLException e) {
            return true;
        }
    }
}
