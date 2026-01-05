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

package dev.brighten.antivpn.message;

import dev.brighten.antivpn.api.APIPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.function.Function;

@Getter
public class VpnString {
    private final String key;
    private final String defaultMessage;
    private String message;
    @Setter
    private Function<VpnString, String> configStringGetter;

    public VpnString(String key, String defaultMessage) {
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

    @SneakyThrows
    public void updateString() {
        if(configStringGetter == null) throw new Exception("The configStringGetter for string " + key + " is null!");

        message = configStringGetter.apply(this);
    }

    public String getFormattedMessage(Var<String, Object>... replacements) {
        String formatted = configStringGetter.apply(this);

        for (Var<String, Object> replacement : replacements) {
            formatted = formatted
                    .replace("%" + replacement.getKey() + "%", replacement.getReplacement().toString());
        }

        return formatted;
    }

    public void sendMessage(APIPlayer player, Var<String, Object>... replacements) {
        String formatted = message;

        for (Var<String, Object> replacement : replacements) {
            formatted = formatted
                    .replace("%" + replacement.getKey() + "%", replacement.getReplacement().toString());
        }
        player.sendMessage(formatted);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Var<S, O> {
        private final String key;
        private final Object replacement;
    }
}
