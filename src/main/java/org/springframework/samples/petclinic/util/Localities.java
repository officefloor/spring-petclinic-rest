/*
 * Copyright 2016 the original author or authors.
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
 * Derives the canonical region ('locality') for a city from a fixed city-to-region table.
 *
 * <p>The table pins {@code Sydney -> NSW}, {@code Melbourne -> VIC} and
 * {@code Brisbane -> QLD}. Any city not in the table (including {@code null})
 * derives the locality {@code "UNKNOWN"}.
 */
public abstract class Localities {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region keyed by the leading two digits of its postcode range (20xx, 30xx, 40xx). */
    private static final Map<Integer, String> POSTCODE_REGION =
        Map.of(20, "NSW", 30, "VIC", 40, "QLD");

    /**
     * @param city the owner's city, may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String regionOf(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Prefers the postcode: a 4-digit postcode in a known range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) fixes the region. Otherwise falls back to the city-to-region table.
     *
     * @param city the owner's city, may be {@code null}
     * @param postcode the owner's postcode, may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when neither resolves
     */
    public static String regionOf(String city, String postcode) {
        String byPostcode = regionOfPostcode(postcode);
        return byPostcode != null ? byPostcode : regionOf(city);
    }

    private static String regionOfPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        return POSTCODE_REGION.get(Integer.parseInt(postcode) / 100);
    }

}
