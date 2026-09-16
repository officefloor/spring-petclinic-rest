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
 * The pinned region-to-postcode-range table used to validate an owner's postcode against the
 * region derived from their city (see {@link Locality}). Each region admits a contiguous,
 * inclusive band of 4-digit postcodes; a city whose region is not in the table accepts any
 * 4-digit postcode.
 */
public final class PostcodeRange {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private PostcodeRange() {
    }

    /**
     * Whether the given 4-digit postcode is acceptable for the region of the given city.
     *
     * @param city     the owner's city (may be {@code null}); its region is resolved via
     *                 {@link Locality#forCity(String)}.
     * @param postcode a 4-digit postcode string; must be non-null and already format-valid.
     * @return {@code true} when the city's region is unknown (any 4-digit postcode is accepted)
     *         or the postcode falls within the region's inclusive range; {@code false} otherwise.
     */
    public static boolean isValidForCity(String city, String postcode) {
        int[] range = REGION_RANGE.get(Locality.forCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * The region whose inclusive range contains the given postcode.
     *
     * @param postcode a postcode string; may be {@code null} or not 4 digits.
     * @return the region string, or {@code null} when the postcode is absent, not a 4-digit
     *         number, or falls in no region's range.
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
