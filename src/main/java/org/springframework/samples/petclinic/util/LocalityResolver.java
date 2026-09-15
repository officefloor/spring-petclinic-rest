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

package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Resolves an owner's locality (canonical region). The postcode is preferred: a postcode
 * falling in a known region's range fixes the region directly, which disambiguates cities
 * that share a name. Only when the postcode is absent or in no known range does resolution
 * fall back to the fixed city-to-region table; cities absent from that table (and postcodes
 * in no known range) resolve to {@link #UNKNOWN}.
 */
public abstract class LocalityResolver {

    /** Locality returned when neither the postcode nor the city resolves to a region. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Return the canonical region for the given postcode and city, preferring the postcode:
     * a postcode within a known region's range yields that region, otherwise the city-to-region
     * table is consulted. Yields {@link #UNKNOWN} when neither resolves to a region.
     */
    public static String localityOf(String postcode, String city) {
        String byPostcode = regionForPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Return the canonical region for the given city alone (no postcode), or {@link #UNKNOWN}
     * when the city is {@code null} or not in the table.
     */
    public static String localityOf(String city) {
        return localityOf(null, city);
    }

    /**
     * Return the region whose range contains the given postcode, or {@code null} when the
     * postcode is {@code null}, non-numeric, or in no known region's range.
     */
    private static String regionForPostcode(String postcode) {
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
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return the inclusive {@code {low, high}} postcode range for the given region, or
     * {@code null} when the region is unknown. A defensive copy is returned so callers
     * cannot mutate the shared table.
     */
    public static int[] rangeForRegion(String region) {
        int[] range = REGION_RANGE.get(region);
        return range == null ? null : range.clone();
    }

    /**
     * Whether the given locality is a known canonical region (one of NSW, VIC or QLD),
     * as opposed to {@link #UNKNOWN} or any other value.
     */
    public static boolean isKnownRegion(String region) {
        return REGION_RANGE.containsKey(region);
    }

}
