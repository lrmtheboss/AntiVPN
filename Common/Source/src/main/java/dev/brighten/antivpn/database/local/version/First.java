package dev.brighten.antivpn.database.local.version;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.local.H2VPN;
import dev.brighten.antivpn.database.sql.utils.Query;
import dev.brighten.antivpn.database.version.H2Version;

public class First implements H2Version {
    @Override
    public void update(H2VPN database) throws Exception {
        Query.prepare("create table if not exists `whitelisted` (`uuid` varchar(36) not null)").execute();
        Query.prepare("create table if not exists `whitelisted-ips` (`ip` varchar(45) not null)").execute();
        Query.prepare("create table if not exists `responses` (`ip` varchar(45) not null, `asn` varchar(12),"
                + "`countryName` text, `countryCode` varchar(10), `city` text, `timeZone` varchar(64), "
                + "`method` varchar(32), `isp` text, `proxy` boolean, `cached` boolean, `inserted` timestamp,"
                + "`latitude` double, `longitude` double)").execute();
        Query.prepare("create table if not exists `alerts` (`uuid` varchar(36) not null)").execute();

        AntiVPN.getInstance().getExecutor().log("Creating indexes...");
        try {
            Query.prepare("create index if not exists `uuid_1` on `whitelisted` (`uuid`)").execute();
            Query.prepare("create index if not exists `ip_1` on `responses` (`ip`)").execute();
            Query.prepare("create index if not exists `proxy_1` on `responses` (`proxy`)").execute();
            Query.prepare("create index if not exists `inserted_1` on `responses` (`inserted`)").execute();
            Query.prepare("create index if not exists `ip_1` on `whitelisted-ips` (`ip`)").execute();
        } catch (Exception e) {
            System.err.println("MySQL Excepton created" + e.getMessage());
        }
    }

    @Override
    public int versionNumber() {
        return 0;
    }

    @Override
    public boolean needsUpdate(H2VPN database) {
        return false;
    }
}
