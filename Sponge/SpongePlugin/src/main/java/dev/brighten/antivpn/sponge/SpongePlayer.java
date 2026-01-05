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

package dev.brighten.antivpn.sponge;

import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.sponge.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SpongePlayer extends APIPlayer {

    private final ServerPlayer player;

    public SpongePlayer(ServerPlayer player) {
        super(player.uniqueId(), player.name(), player.connection().address().getAddress());
        this.player = player;
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(Component.text(StringUtil.translateColorCodes('&', message)));
    }

    @Override
    public void kickPlayer(String reason) {
        player.kick(Component.text(StringUtil.translateColorCodes('&', reason)));
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }
}
