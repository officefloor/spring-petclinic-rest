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
import java.util.Optional;

/**
 * Decides whether a 4-digit postcode is valid for a city, and reverse-maps a postcode
 * to its region, using a fixed table of inclusive postcode ranges keyed by region
 * (as resolved by {@link LocalityResolver}). A city whose region is unknown accepts any
 * 4-digit postcode.
 */
public final class PostcodePolicy {

    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodePolicy() {
    }

    /**
     * Check whether the given 4-digit postcode falls within the range allowed for the
     * city's region.
     *
     * @param city the owner's city, may be {@code null}
     * @param postcode a 4-digit postcode string
     * @return {@code true} when the city's region is unknown, or the postcode is within
     * the region's inclusive range; {@code false} otherwise
     */
    public static boolean isValidForCity(String city, String postcode) {
        int[] range = REGION_POSTCODE_RANGE.get(LocalityResolver.regionForCity(city));
        if (range == null) {
            return true;
        }
        return inRange(Integer.parseInt(postcode), range);
    }

    /**
     * Reverse-map a postcode to the region whose range contains it.
     *
     * @param postcode a postcode string, may be {@code null} or non-numeric
     * @return the matching region, or {@link Optional#empty()} when the postcode is
     * absent, non-numeric, or in no known range
     */
    public static Optional<String> regionForPostcode(String postcode) {
        if (postcode == null) {
            return Optional.empty();
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return Optional.empty();
        }
        return REGION_POSTCODE_RANGE.entrySet().stream()
            .filter(entry -> inRange(value, entry.getValue()))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    private static boolean inRange(int value, int[] range) {
        return value >= range[0] && value <= range[1];
    }
}
