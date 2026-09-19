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
 * Decides whether a 4-digit postcode is valid for a city, using a fixed table of
 * inclusive postcode ranges keyed by the city's region (as resolved by
 * {@link LocalityResolver}). A city whose region is unknown accepts any 4-digit postcode.
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
        int[] range = REGION_POSTCODE_RANGE.get(LocalityResolver.resolve(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
