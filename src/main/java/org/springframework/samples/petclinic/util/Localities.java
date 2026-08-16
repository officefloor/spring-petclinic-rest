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
 * derived by preferring the owner's postcode: a postcode that falls in a known region's range decides
 * the region, and only when the postcode is absent or in no known range does the fixed city-to-region
 * table decide it. A city that is not in the table (and no matching postcode) has the locality
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
     * Fixed region-to-postcode-range table: a 4-digit postcode within a region's inclusive range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}) derives that region.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region-to-timezone table: each canonical region maps to its IANA timezone name
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}).
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

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

    /**
     * Returns the canonical region for an owner, preferring the postcode. The region is looked up by
     * postcode range first ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}); only
     * when the postcode is absent, not a 4-digit number, or in no known range does it fall back to the
     * {@link #region(String) city-to-region table}. For a known city with a matching postcode this
     * returns the same region as the city alone, but a postcode disambiguates cities that share a name.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @param city the owner's city
     * @return the canonical region string, or {@code "UNKNOWN"} when neither postcode nor city resolves
     */
    public static String region(String postcode, String city) {
        String fromPostcode = regionForPostcode(postcode);
        return fromPostcode != null ? fromPostcode : region(city);
    }

    /**
     * Returns the IANA timezone name for an owner's locality, derived from the region resolved by
     * {@link #region(String, String)} via the fixed region-to-timezone table
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}), or {@code null} when the region has no known timezone
     * (including the {@link #UNKNOWN} locality).
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @param city the owner's city
     * @return the IANA timezone name, or {@code null} when the locality has no known timezone
     */
    public static String timezone(String postcode, String city) {
        return REGION_TIMEZONE.get(region(postcode, city));
    }

    /**
     * Returns the region whose inclusive range contains the given postcode, or {@code null} when the
     * postcode is {@code null}, not a 4-digit number, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
