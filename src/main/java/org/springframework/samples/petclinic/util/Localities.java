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
 * Helpers for the "locality" (region) a pet owner belongs to. The locality is the canonical region
 * looked up from a fixed city-to-region table; a city that is not in the table has the locality
 * {@code "UNKNOWN"}.
 */
public final class Localities {

    /**
     * Fixed city-to-region table. Any city not listed derives the locality {@link #UNKNOWN}.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Locality returned for any city not present in the {@link #CITY_REGION fixed table}.
     */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * Returns the canonical region for the given city from the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}), or
     * {@link #UNKNOWN} when the city ({@code null} included) is not in the table.
     *
     * @param city the owner's city
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
