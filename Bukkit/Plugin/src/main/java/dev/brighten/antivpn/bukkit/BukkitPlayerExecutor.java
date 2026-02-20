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

package dev.brighten.antivpn.bukkit;

import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.api.PlayerExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BukkitPlayerExecutor implements PlayerExecutor {

    private final Map<UUID, BukkitPlayer> cachedPlayers = new HashMap<>();

    @Override
    public Optional<APIPlayer> getPlayer(String name) {
        final Player player = Bukkit.getPlayer(name);

        if(player == null) {
            return Optional.empty();
        }

        return Optional.of(cachedPlayers.computeIfAbsent(player.getUniqueId(), k -> new BukkitPlayer(player)));
    }

    @Override
    public Optional<APIPlayer> getPlayer(UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if(player == null) {
            return Optional.empty();
        }

        return Optional.of(cachedPlayers.computeIfAbsent(player.getUniqueId(), k -> new BukkitPlayer(player)));
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        cachedPlayers.remove(uuid);
    }


    @Override
    public List<APIPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(pl -> cachedPlayers.computeIfAbsent(pl.getUniqueId(), k -> new BukkitPlayer(pl)))
                .collect(Collectors.toList());
    }

}
