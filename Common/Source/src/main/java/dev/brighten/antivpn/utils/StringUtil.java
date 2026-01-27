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

package dev.brighten.antivpn.utils;

import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.web.objects.VPNResponse;

public class StringUtil {
    public static String line(String color) {
        return color + "&m-----------------------------------------------------";
    }

    public static String line() {
        return "&m-----------------------------------------------------";
    }

    public static String varReplace(String input, APIPlayer player, VPNResponse result) {
        return translateAlternateColorCodes('&', input.replace("%player%", player.getName())
                .replace("%reason%", result.getMethod())
                .replace("%country%", result.getCountryName())
                .replace("%city%", result.getCity()));
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }
}
