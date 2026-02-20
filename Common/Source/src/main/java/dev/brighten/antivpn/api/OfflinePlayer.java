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

package dev.brighten.antivpn.api;

import java.net.InetAddress;
import java.util.UUID;

public class OfflinePlayer extends APIPlayer {

    public OfflinePlayer(UUID uuid, String name, InetAddress ip) {
        super(uuid, name, ip);
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void kickPlayer(String reason) {

    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }
}
