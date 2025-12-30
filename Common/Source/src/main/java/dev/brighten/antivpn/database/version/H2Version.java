package dev.brighten.antivpn.database.version;

import dev.brighten.antivpn.database.local.H2VPN;

import java.util.ArrayList;
import java.util.List;

public interface H2Version extends Version<H2VPN> {

    List<H2Version> versions = new ArrayList<>();

    static void registerVersions() {

    }
}
