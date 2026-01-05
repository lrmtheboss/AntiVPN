package dev.brighten.antivpn.database;

import dev.brighten.antivpn.utils.CIDRUtils;
import dev.brighten.antivpn.web.objects.VPNResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface VPNDatabase {
    Optional<VPNResponse> getStoredResponse(String ip);

    void cacheResponse(VPNResponse toCache);

    void deleteResponse(String ip);

    boolean isWhitelisted(UUID uuid);

    boolean isWhitelisted(String ip);

    boolean isWhitelisted(CIDRUtils cidr);

    void addWhitelist(UUID uuid);

    void removeWhitelist(UUID uuid);

    void addWhitelist(CIDRUtils cidr);

    void removeWhitelist(CIDRUtils cidr);

    List<UUID> getAllWhitelisted();

    List<CIDRUtils> getAllWhitelistedIps();

    void alertsState(UUID uuid, Consumer<Boolean> result);

    void updateAlertsState(UUID uuid, boolean state);

    void clearResponses();

    void init();

    void shutdown();
}