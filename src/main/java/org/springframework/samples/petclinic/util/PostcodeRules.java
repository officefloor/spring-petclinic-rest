/*
 * Copyright 2002-2017 the original author or authors.
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
 * Single source of truth for the fixed, lookup-based postcode rule: a 4-digit postcode, when
 * present, must fall within the inclusive range of the region derived from the owner's city.
 *
 * <p>The region is looked up from the owner's city via {@link CityRegions}, the shared city-to-region
 * table; each region owns an inclusive 4-digit range ({@code NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099}). A city with no known region accepts any 4-digit postcode. Postcode is optional:
 * an absent value is always accepted. Four-digit formatting is enforced separately by the
 * {@code @Pattern} constraint on the inbound DTO, so this only decides whether a present,
 * well-formed postcode is in range for its city.
 */
public final class PostcodeRules {

    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodeRules() {
    }

    /**
     * Decide whether {@code postcode} is acceptable for {@code city}.
     *
     * @param city     the owner's city (may be {@code null})
     * @param postcode the inbound postcode (may be {@code null})
     * @return {@code true} when the postcode is absent, is not a 4-digit value (left to
     * {@code @Pattern}), the city has no known region, or the postcode is within the city's region
     * range; {@code false} only when a 4-digit postcode is out of range for the city's known region
     */
    public static boolean isValidForCity(String city, String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return true;
        }
        String region = CityRegions.regionOf(city);
        if (region == null) {
            return true;
        }
        int[] range = REGION_POSTCODES.get(region);
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
