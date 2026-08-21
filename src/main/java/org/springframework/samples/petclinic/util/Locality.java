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
import java.util.regex.Pattern;

/**
 * Derives an owner's locality (region). The postcode is preferred: the region is
 * looked up by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099),
 * and only when the postcode is absent or falls in no known range does derivation
 * fall back to the fixed city-to-region table. Anything unresolved is
 * {@code "UNKNOWN"}.
 */
public final class Locality {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** City -> canonical region; anything not listed resolves to "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Value returned for any locality that cannot be resolved. */
    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /**
     * @param city the owner's city (may be {@code null}).
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     *         city is not in the fixed table.
     */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Derives the region preferring the postcode: the region whose range contains
     * {@code postcode} wins, and only when the postcode is absent or in no known
     * range does the city-to-region table decide.
     *
     * @param postcode the owner's postcode (may be {@code null} or blank).
     * @param city     the owner's city (may be {@code null}).
     * @return the canonical region, or {@code "UNKNOWN"} when neither resolves.
     */
    public static String of(String postcode, String city) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
    private static String byPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String trimmed = postcode.trim();
        if (!FOUR_DIGITS.matcher(trimmed).matches()) {
            return null;
        }
        int value = Integer.parseInt(trimmed);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
