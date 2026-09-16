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
 * Fixed lookup of a region to its inclusive 4-digit postcode range.
 */
public final class RegionPostcodes {

    private static final Map<String, int[]> REGION_TO_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private RegionPostcodes() {
    }

    /**
     * Return whether the given postcode is acceptable for the given region: {@code true} when the
     * region has no known range (any 4-digit postcode is allowed) or the postcode falls within the
     * region's inclusive range.
     *
     * @param region   the canonical region (see {@link CityRegions})
     * @param postcode the 4-digit postcode as an integer
     * @return {@code true} if the postcode is acceptable for the region
     */
    public static boolean isValidForRegion(String region, int postcode) {
        int[] range = REGION_TO_RANGE.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }
}
