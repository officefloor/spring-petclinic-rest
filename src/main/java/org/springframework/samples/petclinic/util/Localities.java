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
 * Resolves an owner's locality (region) from their city using a fixed lookup table.
 *
 * <p>The mapping is pinned to Sydney-&gt;NSW, Melbourne-&gt;VIC and Brisbane-&gt;QLD. Any
 * city outside the table resolves to {@code UNKNOWN}.
 */
public final class Localities {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGE =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Localities() {
    }

    /** IANA timezone for {@code region} from the pinned table, or null when it is not one of NSW/VIC/QLD. */
    public static String timezoneFor(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /** Return the canonical region for {@code city}, or {@code UNKNOWN} when it is not in the table. */
    public static String regionFor(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** Prefer the region from the postcode range; fall back to the city table when the postcode is absent or unknown. */
    public static String regionFor(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : regionFor(city);
    }

    /** Region whose range contains {@code postcode}, or {@code null} when it is absent, malformed, or out of every range. */
    private static String regionForPostcode(String postcode) {
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
