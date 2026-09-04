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
 * Derives an owner's {@code locality} from its city using a fixed city-to-region
 * table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD).
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    /**
     * @param city the owner's city
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     *         city is not in the table
     */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Prefers the postcode: resolves the region by postcode range first (NSW
     * 2000-2099, VIC 3000-3099, QLD 4000-4099), and only falls back to the
     * city-to-region table when the postcode is absent or in no known range.
     *
     * @param city     the owner's city
     * @param postcode the owner's postcode, may be {@code null}
     * @return the canonical region, or {@code "UNKNOWN"} when neither resolves
     */
    public static String of(String city, String postcode) {
        String region = byPostcode(postcode);
        return region != null ? region : of(city);
    }

    private static String byPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        return REGION_POSTCODES.entrySet().stream()
            .filter(e -> value >= e.getValue()[0] && value <= e.getValue()[1])
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }
}
