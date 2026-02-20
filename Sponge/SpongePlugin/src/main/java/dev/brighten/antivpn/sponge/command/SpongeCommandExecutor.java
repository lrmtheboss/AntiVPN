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

package dev.brighten.antivpn.sponge.command;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.command.CommandExecutor;
import dev.brighten.antivpn.sponge.util.StringUtil;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

@RequiredArgsConstructor
public class SpongeCommandExecutor implements CommandExecutor {

    private final CommandCause cause;

    @Override
    public void sendMessage(String message, Object... objects) {
        cause.sendMessage(Component.text(StringUtil.translateColorCodes('&',
                String.format(message, objects))));
    }

    @Override
    public boolean hasPermission(String permission) {
        return cause.hasPermission(permission);
    }

    @Override
    public Optional<APIPlayer> getPlayer() {
        if(cause.subject() instanceof ServerPlayer serverPlayer) {
            return AntiVPN.getInstance().getPlayerExecutor().getPlayer(serverPlayer.uniqueId());
        }
        return Optional.empty();
    }

    @Override
    public boolean isPlayer() {
        return cause.subject() instanceof ServerPlayer;
    }
}
