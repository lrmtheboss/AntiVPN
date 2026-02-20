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

package dev.brighten.antivpn.bungee;

import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.api.PlayerExecutor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class BungeePlayerExecutor implements PlayerExecutor {

    private final Map<UUID, BungeePlayer> cachedPlayers = new HashMap<>();

    @Override
    public Optional<APIPlayer> getPlayer(String name) {
        ProxiedPlayer player = BungeePlugin.pluginInstance.getProxy().getPlayer(name);

        if(player == null) return Optional.empty();

        return Optional.of(cachedPlayers.computeIfAbsent(player.getUniqueId(), key -> new BungeePlayer(player)));
    }

    @Override
    public Optional<APIPlayer> getPlayer(UUID uuid) {
        ProxiedPlayer player = BungeePlugin.pluginInstance.getProxy().getPlayer(uuid);

        if(player == null) return Optional.empty();

        return Optional.of(cachedPlayers.computeIfAbsent(uuid, key -> new BungeePlayer(player)));
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        this.cachedPlayers.remove(uuid);
    }

    @Override
    public List<APIPlayer> getOnlinePlayers() {
        return BungeePlugin.pluginInstance.getProxy().getPlayers().stream()
                .map(pl -> cachedPlayers.computeIfAbsent(pl.getUniqueId(), key -> new BungeePlayer(pl)))
                .collect(Collectors.toList());
    }
}
