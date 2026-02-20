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
import lombok.Builder;
import lombok.Data;


/**
 * Used to format the JSON response from <a href="https://funkemunky.cc/vpn/queryCheck">...</a> into an object for project use.
 */
@Data
@Builder(toBuilder = true)
public class QueryResponse {

    private boolean validPlan;
    private String planType;
    private long queries;
    private long queriesMax;

    /**
     * Formats response from <a href="https://funkemunky.cc/vpn/queryCheck">...</a> into {@link QueryResponse} for project use.
     *
     * @param object JSONObject
     * @return QueryResponse
     * @throws JSONException Throws when JSON is not formatted properly.
     */
    public static QueryResponse fromJson(JSONObject object) throws JSONException {
        boolean validPlan = object.getBoolean("validPlan");

        if(!validPlan) { // Nothing else will be returned from API if validPlan is false.
            return QueryResponse.builder().validPlan(false).build();
        }

        return QueryResponse.builder().validPlan(object.getBoolean("validPlan"))
                .planType(object.getString("planType"))
                .queries(object.getLong("queries"))
                .queriesMax(object.getLong("queryLimit")).build();
    }
}
