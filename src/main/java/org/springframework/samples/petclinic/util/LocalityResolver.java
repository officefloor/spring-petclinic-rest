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
 * Derives an owner's 'locality' (canonical region). The postcode is preferred: the region is
 * looked up by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only when
 * the postcode is absent or in no known range does it fall back to a fixed city-to-region table.
 * This returns the same region for known cities but disambiguates cities that share a name. Any
 * input that resolves to neither derives the region {@code "UNKNOWN"}.
 */
public final class LocalityResolver {

    /** Fixed city-to-region table; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Fixed region -> IANA timezone table; anything not listed has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for the given owner, preferring the postcode. The postcode is
     * matched against the known region ranges first; when it is {@code null} or in no known range,
     * the region is derived from the city via {@link #resolve(String)}.
     *
     * @param city     the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null})
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public static String resolve(String city, String postcode) {
        String byPostcode = fromPostcode(postcode);
        return byPostcode != null ? byPostcode : resolve(city);
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

    /**
     * Returns the IANA timezone name for the given region via the fixed region-to-timezone table,
     * or {@code null} when the region is {@code null} or not a known region.
     *
     * @param region the canonical region string (e.g. {@code "NSW"}, may be {@code null})
     * @return the IANA timezone name, or {@code null}
     */
    public static String timezone(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /**
     * Returns the region whose range contains the postcode, or {@code null} when the postcode is
     * {@code null}, not a parseable integer, or in no known range.
     */
    private static String fromPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException e) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
