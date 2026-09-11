/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's 'locality' (canonical region) from their city using a fixed
 * city-to-region table. Any city not listed in the table derives the region
 * {@code "UNKNOWN"}.
 */
public final class LocalityResolver {

    /** Fixed city-to-region table; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for the given city, or {@code "UNKNOWN"} when the city is
     * {@code null} or not present in the fixed city-to-region table.
     *
     * @param city the owner's city (may be {@code null})
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public static String resolve(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
