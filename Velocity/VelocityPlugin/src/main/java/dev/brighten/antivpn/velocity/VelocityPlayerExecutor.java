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

package dev.brighten.antivpn.velocity;

import com.velocitypowered.api.proxy.Player;
import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.api.PlayerExecutor;

import java.util.*;
import java.util.stream.Collectors;

public class VelocityPlayerExecutor implements PlayerExecutor {

    private final Map<UUID, VelocityPlayer> cachedPlayers = new HashMap<>();

    @Override
    public Optional<APIPlayer> getPlayer(String name) {
        Optional<Player> player = VelocityPlugin.INSTANCE.getServer().getPlayer(name);

        return player.map(value -> cachedPlayers.computeIfAbsent(value.getUniqueId(),
                key -> new VelocityPlayer(value)));

    }

    @Override
    public Optional<APIPlayer> getPlayer(UUID uuid) {
        Optional<Player> player = VelocityPlugin.INSTANCE.getServer().getPlayer(uuid);

        return player.map(value -> cachedPlayers.computeIfAbsent(value.getUniqueId(),
                key -> new VelocityPlayer(value)));
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        cachedPlayers.remove(uuid);
    }

    @Override
    public List<APIPlayer> getOnlinePlayers() {
        return VelocityPlugin.INSTANCE.getServer().getAllPlayers().stream()
                .map(pl -> cachedPlayers.computeIfAbsent(pl.getUniqueId(), key -> new VelocityPlayer(pl)))
                .collect(Collectors.toList());
    }
}
