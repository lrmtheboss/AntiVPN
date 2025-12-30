package dev.brighten.antivpn.database.version;

public interface Version<DB> {
    void update(DB database) throws Exception;
    int versionNumber();
    boolean needsUpdate(DB database);
}