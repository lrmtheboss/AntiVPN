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

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.command.CommandExecutor;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

@RequiredArgsConstructor
public class BukkitCommandExecutor implements CommandExecutor {

    private final CommandSender sender;

    @Override
    public void sendMessage(String message, Object... objects) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                String.format(message, objects)));
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public Optional<APIPlayer> getPlayer() {
        if(!isPlayer()) return Optional.empty();

        return AntiVPN.getInstance().getPlayerExecutor().getPlayer(((Player)sender).getUniqueId());
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }
}
