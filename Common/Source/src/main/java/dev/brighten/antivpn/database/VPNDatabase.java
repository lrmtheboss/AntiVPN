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