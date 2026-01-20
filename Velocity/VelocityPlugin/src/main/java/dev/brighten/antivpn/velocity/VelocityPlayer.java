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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityPlayer extends APIPlayer {

    private final Player player;
    public VelocityPlayer(Player player) {
        super(player.getUniqueId(), player.getUsername(), player.getRemoteAddress().getAddress());

        this.player = player;
    }


    @Override
    public void sendMessage(String message) {
        player.sendMessage(LegacyComponentSerializer.builder().character('&').build().deserialize(message));
    }

    @Override
    public void kickPlayer(String reason) {
        player.disconnect(LegacyComponentSerializer.builder().character('&').build().deserialize(reason));
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }


}
