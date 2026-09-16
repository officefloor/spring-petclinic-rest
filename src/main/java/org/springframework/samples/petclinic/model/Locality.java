/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * The pinned city-to-region table used to derive an owner's locality. Maps a city to its
 * canonical region; any city not in the table resolves to {@link #UNKNOWN}.
 */
public final class Locality {

    /** Region returned for a city that is not in the {@link #CITY_REGION} table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private Locality() {
    }

    /**
     * Derive the canonical region for the given city.
     *
     * @param city the city name (may be {@code null}).
     * @return the region string, or {@link #UNKNOWN} when the city is not in the table.
     */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Derive the canonical region, preferring the postcode. The postcode's region range is
     * consulted first (see {@link PostcodeRange#regionForPostcode(String)}); only when the
     * postcode is absent or in no known range does this fall back to the city-to-region table.
     * This yields the same region for the pinned cities but disambiguates cities that share a
     * name.
     *
     * @param city     the city name (may be {@code null}).
     * @param postcode the postcode (may be {@code null}).
     * @return the region string, or {@link #UNKNOWN} when neither the postcode nor the city
     *         resolves to a known region.
     */
    public static String forCityAndPostcode(String city, String postcode) {
        String region = PostcodeRange.regionForPostcode(postcode);
        return region != null ? region : forCity(city);
    }
}
