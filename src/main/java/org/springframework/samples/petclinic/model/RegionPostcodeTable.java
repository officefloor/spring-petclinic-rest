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
package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.Optional;

/**
 * The fixed region-to-postcode-range lookup used to validate an owner's postcode and to
 * resolve the region a postcode belongs to.
 *
 * <p>Maps each known region (see {@link CityRegionTable}) to the inclusive range of
 * 4-digit postcodes valid within it: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
 * A region with no known range accepts any postcode. This is the single source of
 * truth for the mapping, so callers never repeat the table.
 */
public final class RegionPostcodeTable {

    private static final Map<String, int[]> RANGE_BY_REGION = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private RegionPostcodeTable() {
    }

    /**
     * Resolve the canonical region whose postcode range contains the given postcode.
     *
     * @param postcode a postcode value, which may be {@code null} or non-numeric
     * @return the region owning the range that contains the postcode, or an empty
     *         {@link Optional} when the postcode is absent, unparseable or in no known range
     */
    public static Optional<String> regionFor(String postcode) {
        if (postcode == null || !postcode.chars().allMatch(Character::isDigit) || postcode.isEmpty()) {
            return Optional.empty();
        }
        int value = Integer.parseInt(postcode);
        return RANGE_BY_REGION.entrySet().stream()
            .filter(e -> value >= e.getValue()[0] && value <= e.getValue()[1])
            .map(Map.Entry::getKey)
            .findFirst();
    }

    /**
     * Whether the given postcode falls within the range valid for the region.
     *
     * @param region   the canonical region (as returned by {@link CityRegionTable})
     * @param postcode the numeric postcode value
     * @return {@code true} if the region has no known range (permissive default) or the
     *         postcode is within its inclusive range, {@code false} otherwise
     */
    public static boolean accepts(String region, int postcode) {
        int[] range = RANGE_BY_REGION.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }
}
