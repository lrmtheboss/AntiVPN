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

package dev.brighten.antivpn.command.impl;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.command.Command;
import dev.brighten.antivpn.command.CommandExecutor;
import dev.brighten.antivpn.utils.CIDRUtils;
import dev.brighten.antivpn.utils.MiscUtils;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class AllowlistCommand extends Command {

    private static final String[] secondArgs = new String[] {"add", "remove", "show"};

    @Override
    public String permission() {
        return "antivpn.command.allowlist";
    }

    @Override
    public String name() {
        return "allowlist";
    }

    @Override
    public String[] aliases() {
        return new String[] {"whitelist"};
    }

    @Override
    public String description() {
        return "Add/remove players to/from exemption list.";
    }

    @Override
    public String usage() {
        return "<add <player/uuid/ip> | remove <player/uuid/ip> | show [search]>";
    }

    @Override
    public String parent() {
        return "antivpn";
    }

    @Override
    public Command[] children() {
        return new Command[0];
    }

    @Override
    public String execute(CommandExecutor executor, String[] args) {
        if(args.length == 0 || Arrays.stream(secondArgs).noneMatch(arg -> arg.equalsIgnoreCase(args[0]))) {
            return "&cUsage: /antivpn allowlist " + usage();
        }

        if(args[0].equalsIgnoreCase("show")) {
            String search = args.length > 1 ? args[1].toLowerCase() : null;
            // Strip color code characters to prevent formatting injection in output
            String safeSearch = search != null ? search.replace("&", "") : null;
            boolean databaseEnabled = AntiVPN.getInstance().getVpnConfig().isDatabaseEnabled();

            AntiVPN.getInstance().getExecutor().log("Is Database Enabled: %s", databaseEnabled ? "yes" : "no");

            List<UUID> uuids = databaseEnabled
                    ? AntiVPN.getInstance().getDatabase().getAllWhitelisted()
                    : new ArrayList<>(AntiVPN.getInstance().getExecutor().getWhitelisted());
            List<CIDRUtils> ips = databaseEnabled
                    ? AntiVPN.getInstance().getDatabase().getAllWhitelistedIps()
                    : new ArrayList<>(AntiVPN.getInstance().getExecutor().getWhitelistedIps());

            List<String> messages = new ArrayList<>();
            messages.add("&8&m-----------------------------------------------------");
            messages.add("&6&lAllowlist Entries" + (safeSearch != null ? " &7(search: &f" + safeSearch + "&7)" : ""));
            messages.add("");

            boolean any = false;
            for (UUID uuid : uuids) {
                String entry = uuid.toString();
                if (search == null || entry.toLowerCase().contains(search)) {
                    messages.add("&7- &fUUID: &e" + entry);
                    any = true;
                }
            }
            for (CIDRUtils cidr : ips) {
                String entry = cidr.getCidr();
                if (search == null || entry.toLowerCase().contains(search)) {
                    messages.add("&7- &fIP: &e" + entry);
                    any = true;
                }
            }

            if (!any) {
                messages.add(safeSearch != null
                        ? "&cNo allowlist entries matching &f\"" + safeSearch + "&c\" were found."
                        : "&cThe allowlist is empty.");
            }
            messages.add("&8&m-----------------------------------------------------");
            return String.join("\n", messages);
        }

        if(args.length == 1)
            return "&cYou have to provide a player to allow or deny exemption.";

        boolean databaseEnabled = AntiVPN.getInstance().getVpnConfig().isDatabaseEnabled();

        if(!databaseEnabled) executor.sendMessage("&cThe database is currently not setup, " +
                "so any changes here will disappear after a restart.");

        CIDRUtils cidrUtils;

        try {
            cidrUtils = new CIDRUtils(args[1]);
        } catch(IllegalArgumentException | UnknownHostException e) {
            cidrUtils = null;
        }

        if(cidrUtils != null) {
            if(!databaseEnabled) {
                return switch (args[0].toLowerCase()) {
                    case "add", "insert" -> {
                        AntiVPN.getInstance().getExecutor().getWhitelistedIps().add(cidrUtils);
                        yield String.format("&aAdded &6%s &ato exemption allowlist.", cidrUtils.getCidr());
                    }
                    case "remove", "delete" -> {
                        AntiVPN.getInstance().getExecutor().getWhitelistedIps().remove(cidrUtils);
                        yield String.format("&cRemoved &%s &cfrom the exemption allowlist.", cidrUtils.getCidr());
                    }
                    default -> "&c\"" + args[0] + "\" is not a valid argument";
                };
            } else return switch (args[0].toLowerCase()) {
                case "add", "insert" -> {
                    AntiVPN.getInstance().getExecutor().getWhitelistedIps().add(cidrUtils);
                    AntiVPN.getInstance().getDatabase().addWhitelist(cidrUtils);
                    yield String.format("&aAdded &6%s &ato exemption allowlist.", cidrUtils.getCidr());
                }
                case "remove", "delete" -> {
                    AntiVPN.getInstance().getExecutor().getWhitelistedIps().remove(cidrUtils);
                    AntiVPN.getInstance().getDatabase().removeWhitelist(cidrUtils);
                    yield String.format("&cRemoved &6%s &cfrom the exemption allowlist.", cidrUtils.getCidr());
                }
                default -> "&c\"" + args[0] + "\" is not a valid argument";
            };
        }
        if(MiscUtils.isIpv4(args[1])) {
            if(!databaseEnabled) {
                try {
                    return switch(args[0].toLowerCase()) {
                        case "add", "insert" -> {
                            AntiVPN.getInstance().getExecutor().getWhitelistedIps().add(new CIDRUtils(args[1] + "/32"));
                            AntiVPN.getInstance().getDatabase().addWhitelist(new CIDRUtils(args[1] + "/32"));
                            yield String.format("&aAdded &6%s &ato the exemption allowlist.", args[1] + "/32");
                        }
                        case "remove", "delete" -> {
                            AntiVPN.getInstance().getExecutor().getWhitelistedIps().remove(new CIDRUtils(args[1] + "/32"));
                            AntiVPN.getInstance().getDatabase().removeWhitelist(new CIDRUtils(args[1] + "/32"));
                            yield String.format("&cRemoved &6%s &cfrom the exemption allowlist.", args[1] + "/32");
                        }
                        default -> "&c\"" + args[0] + "\" is not a valid argument";
                    };
                } catch (UnknownHostException e) {
                    AntiVPN.getInstance().getExecutor().logException("Invalid IP format for allowlist command", e);
                    return "&cInvalid IP format for allowlist command";
                }
            } else {
                try {
                    return switch (args[0].toLowerCase()) {
                        case "add", "insert" -> {
                            AntiVPN.getInstance().getDatabase().addWhitelist(new CIDRUtils(args[1] + "/32"));
                            yield String.format("&aAdded &6%s &a to the exemption allowlist.", args[1] + "/32");
                        }
                        case "remove", "delete" -> {
                            AntiVPN.getInstance().getDatabase().removeWhitelist(new CIDRUtils(args[1] + "/32"));
                            yield String.format("&cRemoved &6%s &c from the exemption allowlist.", args[1] + "/32");
                        }
                        default -> "&c\"" + args[0] + "\" is not a valid argument";
                    };
                } catch (UnknownHostException e) {
                    AntiVPN.getInstance().getExecutor().logException("Invalid IP format for allowlist command", e);
                    return "&cInvalid IP format for allowlist command";
                }
            }
        } else {
            UUID uuid;
            try {
                uuid = UUID.fromString(args[1]);
            } catch(IllegalArgumentException e) {
                Optional<APIPlayer> player = AntiVPN.getInstance().getPlayerExecutor().getPlayer(args[1]);

                if(player.isEmpty()) {
                    return "&cThe player \"" + args[1] + "\" is not online, so please provide a UUID.";
                }

                uuid = player.get().getUuid();
            }

            if(!databaseEnabled) {
                return switch (args[0].toLowerCase()) {
                    case "add" -> {
                        AntiVPN.getInstance().getExecutor().getWhitelisted().add(uuid);
                        yield String.format("&aAdded &6%s &auuid to the exemption allowlist.", uuid.toString());
                    }
                    case "remove", "delete" -> {
                        AntiVPN.getInstance().getExecutor().getWhitelisted().remove(uuid);
                        yield String.format("&cRemoved &6%s &cuuid from the exemption allowlist.", uuid.toString());
                    }
                    default -> "&c\"" + args[0] + "\" is not a valid argument";
                };
            } else {
                return switch (args[0].toLowerCase()) {
                    case "add" -> {
                        AntiVPN.getInstance().getDatabase().addWhitelist(uuid);
                        yield String.format("&aAdded &6%s &auuid to the exemption allowlist.", uuid.toString());
                    }
                    case "remove", "delete" -> {
                        AntiVPN.getInstance().getDatabase().removeWhitelist(uuid);
                        yield String.format("&cRemoved &6%s &cuuid from the exemption allowlist.", uuid.toString());
                    }
                    default -> "&c\"" + args[0] + "\" is not a valid argument";
                };
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandExecutor executor, String alias, String[] args) {
        return switch (args.length) {
            case 1 -> Arrays.stream(secondArgs)
                    .filter(narg -> narg.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            case 2 -> {
                if (args[0].equalsIgnoreCase("show")) {
                    yield Collections.emptyList();
                }
                yield AntiVPN.getInstance().getPlayerExecutor().getOnlinePlayers().stream()
                        .map(APIPlayer::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            default -> Collections.emptyList();
        };
    }
}
