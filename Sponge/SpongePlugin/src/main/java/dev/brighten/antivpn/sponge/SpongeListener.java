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

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.api.*;
import dev.brighten.antivpn.sponge.util.StringUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class SpongeListener extends VPNExecutor {

    @Listener(order = Order.EARLY)
    public void onJoin(ServerSideConnectionEvent.Login event) {
        AtomicReference<APIPlayer> player = new AtomicReference<>(AntiVPN.getInstance().getPlayerExecutor()
                .getPlayer(event.profile().uuid())
                .orElse(new OfflinePlayer(
                        event.profile().uuid(),
                        event.profile().name().orElse("Unknown"),
                        event.connection().address().getAddress()
                )));

        player.get().checkPlayer(result -> {
            if(!result.resultType().isShouldBlock()) return;

            if(!AntiVPN.getInstance().getVpnConfig().isKickPlayers()) {
                return;
            }

            if(result.isFromCache()) {
                AntiVPN.getInstance().getExecutor().log(Level.INFO, "%s was kicked from cache with IP %s", player.get().getName(), result.response().getIp());
            }

            event.setCancelled(true);
            switch (result.resultType()) {
                case DENIED_PROXY -> {
                    AntiVPN.getInstance().getExecutor().log(Level.INFO, player.get().getName()
                            + " joined on a VPN/Proxy (" + result.response().getMethod() + ")");
                    event.setMessage(Component.text(StringUtil
                            .translateColorCodes('&', AntiVPN.getInstance().getVpnConfig()
                                    .getKickMessage()
                                    .replace("%player%", player.get().getName())
                                    .replace("%country%", result.response().getCountryName())
                                    .replace("%code%", result.response().getCountryCode()))));
                }
                case DENIED_COUNTRY ->
                        event.setMessage(Component.text(StringUtil
                                .translateColorCodes('&', AntiVPN.getInstance().getVpnConfig()
                                        .getCountryVanillaKickReason()
                                        .replace("%player%", player.get().getName())
                                        .replace("%country%", result.response().getCountryName())
                                        .replace("%code%", result.response().getCountryCode()))));
            }
        });
    }

    @Listener
    public void onPlayerDisconnect(ServerSideConnectionEvent.Disconnect event) {
        event.profile().ifPresent(profile ->
                AntiVPN.getInstance().getPlayerExecutor().unloadPlayer(profile.uuid()));
    }

    @Override
    public void registerListeners() {
        Sponge.eventManager().registerListeners(SpongePlugin.getInstance().getContainer(), this);
    }

    @Override
    public void log(Level level, String log, Object... objects) {
        if (level.equals(Level.SEVERE)) {
            SpongePlugin.getInstance().getLogger().error(String.format(log, objects));
        } else if (level.equals(Level.WARNING)) {
            SpongePlugin.getInstance().getLogger().warn(String.format(log, objects));
        } else {
            SpongePlugin.getInstance().getLogger().info(String.format(log, objects));
        }
    }

    @Override
    public void log(String log, Object... objects) {
        log(Level.INFO, String.format(log, objects));
    }

    @Override
    public void logException(String message, Throwable ex) {
        SpongePlugin.getInstance().getLogger().error(message, ex);
    }

    @Override
    public void runCommand(String command) {
        try {
            Sponge.server().commandManager().process(Sponge.systemSubject(), command);
        } catch (CommandException e) {
            logException(e);
        }
    }

    @Override
    public void disablePlugin() {
        AntiVPN.getInstance().getExecutor().log(Level.INFO, "Disabling listeners for plugin...");
    }
}
