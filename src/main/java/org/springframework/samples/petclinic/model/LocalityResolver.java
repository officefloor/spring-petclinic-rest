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
 * Resolves an owner's locality (canonical region) from their postcode and city. The postcode is
 * preferred: a postcode falling within a known region's range determines the region directly, which
 * disambiguates cities that share a name. Only when the postcode is absent or in no known range does
 * resolution fall back to the fixed city-to-region table. Inputs matching neither resolve to
 * {@link #UNKNOWN}.
 */
public final class LocalityResolver {

    /** Locality returned when neither the postcode nor the city yields a region. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive four-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONES = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private LocalityResolver() {
    }

    /**
     * Resolve the canonical region, preferring the postcode over the city.
     *
     * @param postcode the postcode to look up first, or {@code null}/absent
     * @param city     the city used as a fallback when the postcode yields no region
     * @return the region determined by the postcode's range, or failing that by the
     *         city-to-region table, or {@link #UNKNOWN} when neither matches
     */
    public static String regionFor(String postcode, String city) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : regionForCity(city);
    }

    /**
     * Resolve the canonical region for the given city via the fixed city-to-region table.
     *
     * @param city the city to look up
     * @return the region string, or {@link #UNKNOWN} when the city is not in the table
     */
    public static String regionForCity(String city) {
        return CITY_REGIONS.getOrDefault(city, UNKNOWN);
    }

    /**
     * Whether the given region is one of the known canonical regions (i.e. has a defined postcode
     * range), as opposed to {@link #UNKNOWN} or any other unrecognised value.
     *
     * @param region the region to test, or {@code null}
     * @return {@code true} when the region is a known region
     */
    public static boolean isKnownRegion(String region) {
        return region != null && REGION_RANGES.containsKey(region);
    }

    /**
     * The IANA timezone name for the given region via the fixed region-to-timezone table.
     *
     * @param region the region to look up
     * @return the IANA timezone name, or {@code null} when the region has no known timezone
     */
    public static String timezoneFor(String region) {
        return REGION_TIMEZONES.get(region);
    }

    /**
     * The inclusive four-digit postcode range for the given region.
     *
     * @param region the region to look up
     * @return a fresh {@code {low, high}} array, or {@code null} when the region has no range
     */
    public static int[] rangeForRegion(String region) {
        int[] range = REGION_RANGES.get(region);
        return range == null ? null : range.clone();
    }

    /**
     * The region whose postcode range contains the given postcode.
     *
     * @param postcode the postcode to classify, or {@code null}/absent
     * @return the matching region, or {@code null} when the postcode is absent, not a number, or in
     *         no known range
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
