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

import lombok.Getter;

public enum ResultType {
    ALLOWED(false),
    WHITELISTED(false),
    DENIED_COUNTRY(true),
    DENIED_PROXY(true),
    API_FAILURE(false),
    UNKNOWN(false);

    @Getter
    private final boolean shouldBlock;

    ResultType(boolean shouldBlock) {
        this.shouldBlock = shouldBlock;
    }
}
