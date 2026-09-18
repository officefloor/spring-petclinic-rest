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
import java.util.Optional;

/**
 * Fixed lookup from a region code to its inclusive 4-digit postcode range.
 */
public final class RegionPostcodeTable {

    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private RegionPostcodeTable() {
    }

    /**
     * Whether the given numeric postcode is acceptable for the region: within the
     * region's inclusive range when the region is known, and any value when the region
     * has no range in the table.
     */
    public static boolean accepts(String region, int postcode) {
        int[] range = REGION_RANGES.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }

    /**
     * The region whose inclusive range contains the given postcode, or empty when the
     * postcode is absent, not numeric, or in no known range.
     */
    public static Optional<String> regionOf(String postcode) {
        if (postcode == null || !postcode.matches("\\d+")) {
            return Optional.empty();
        }
        int value = Integer.parseInt(postcode);
        return REGION_RANGES.entrySet().stream()
            .filter(e -> value >= e.getValue()[0] && value <= e.getValue()[1])
            .map(Map.Entry::getKey)
            .findFirst();
    }
}
