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

package dev.brighten.antivpn.web.objects;

import dev.brighten.antivpn.utils.json.JSONException;
import dev.brighten.antivpn.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class VPNResponse {
    private String asn, ip, countryName, countryCode, city, timeZone, method, isp, failureReason = "N/A";
    private boolean proxy, cached;
    private final boolean success;
    private double latitude, longitude;
    private long lastAccess;
    private long queriesLeft;

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("ip", ip);
        json.put("countryName", countryName);
        json.put("countryCode", countryCode);
        json.put("city", city);
        json.put("method", method);
        json.put("isp", isp);
        json.put("proxy", proxy);
        json.put("success", success);
        json.put("timeZone", timeZone);
        json.put("queriesLeft", queriesLeft);
        json.put("cached", cached);

        return json;
    }

    /**
     * Feeds into {@link VPNResponse#fromJson(JSONObject)} formatting the JSON {@link String} into
     * a {@link JSONObject}
     *
     * @param json String
     * @return VPNResponse
     */
    public static VPNResponse fromJson(String json) throws JSONException {
        return fromJson(new JSONObject(json));
    }

    public static final VPNResponse FAILED_RESPONSE = VPNResponse.builder()
            .success(false)
            .failureReason("Internal plugin API error.")
            .build();

    /**
     * Formats response from <a href="https://funkemunky.cc/vpn">...</a> into {@link VPNResponse} for project use.
     *
     * @param jsonObject JSONObject
     * @return VPNResponse
     * @throws JSONException Throws when JSON is not formatted properly.
     */
    public static VPNResponse fromJson(JSONObject jsonObject) throws JSONException {
        if(jsonObject.getBoolean("success")) {
            return new VPNResponse(jsonObject.getString("asn"), jsonObject.getString("ip"),
                    jsonObject.getString("countryName"), jsonObject.getString("countryCode"),
                    jsonObject.getString("city"), jsonObject.getString("timeZone"),
                    jsonObject.has("method") ? jsonObject.getString("method") : "N/A",
                    jsonObject.getString("isp"), "N/A", jsonObject.getBoolean("proxy"),
                    jsonObject.getBoolean("cached"), jsonObject.getBoolean("success"),
                    jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"),
                    jsonObject.getLong("lastAccess"), jsonObject.getInt("queriesLeft"));
        } else {
            return VPNResponse.builder().success(false)
                    .failureReason(jsonObject.getString("failureReason")).build();
        }
    }
}
