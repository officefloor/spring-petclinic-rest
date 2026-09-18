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
 * The fixed postcode-range-to-region lookup.
 *
 * <p>Maps each region to its inclusive 4-digit postcode range
 * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}) and
 * answers the two questions the application asks of that single table: which
 * region a postcode falls in, and whether a region has a known range at all.
 * The ranges are disjoint, so a postcode belongs to at most one region.
 */
public final class PostcodeRegion {

    /** Region -> inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodeRegion() {
    }

    /**
     * Return the region whose range contains the given postcode, or {@code null}
     * when the postcode is absent, non-numeric, or in no known range.
     *
     * @param postcode the postcode to look up (may be {@code null} or blank)
     * @return the region string, or {@code null}
     */
    public static String regionFor(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return whether the given region has a known postcode range.
     *
     * @param region the region to check (may be {@code null})
     * @return {@code true} when the region has a range, {@code false} otherwise
     */
    public static boolean hasRange(String region) {
        return REGION_POSTCODES.containsKey(region);
    }
}
