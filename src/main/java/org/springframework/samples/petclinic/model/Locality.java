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
import java.util.Set;

/**
 * Derives an owner's locality — the canonical region. The postcode range is preferred
 * (see {@link RegionPostcodes#regionForPostcode(String)}); only when the postcode is
 * absent or in no known range does a fixed city-to-region table decide. Owners that
 * resolve by neither route yield {@link #UNKNOWN}.
 */
public final class Locality {

    /** Locality returned for any city not present in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_TO_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** The canonical regions this application recognises. */
    private static final Set<String> KNOWN_REGIONS = Set.copyOf(CITY_TO_REGION.values());

    private Locality() {
    }

    /**
     * Whether the given region is one of the canonical known regions (NSW, VIC or QLD).
     * Returns {@code false} for {@code null}, {@link #UNKNOWN} or any other value.
     */
    public static boolean isKnownRegion(String region) {
        return KNOWN_REGIONS.contains(region);
    }

    /**
     * Derives the region for an owner, preferring the postcode range and falling back to
     * the city-to-region table. Returns {@link #UNKNOWN} for a {@code null} owner or when
     * neither route resolves a region.
     */
    public static String forOwner(Owner owner) {
        if (owner == null) {
            return UNKNOWN;
        }
        String byPostcode = RegionPostcodes.regionForPostcode(owner.getPostcode());
        return byPostcode != null ? byPostcode : fromCity(owner.getCity());
    }

    /**
     * Returns the locality recorded in an owner's {@code memberId}, i.e. the leading
     * {@code <REGION>} segment of the {@code '<REGION><FY><HASH8><CHK>'} identity. The region
     * is the run of leading letters before the two-digit fiscal year. Returns {@link #UNKNOWN}
     * when the id is {@code null} or carries no region segment.
     */
    public static String fromMemberId(String memberId) {
        if (memberId == null) {
            return UNKNOWN;
        }
        int end = 0;
        while (end < memberId.length() && Character.isLetter(memberId.charAt(end))) {
            end++;
        }
        return end == 0 ? UNKNOWN : memberId.substring(0, end);
    }

    /**
     * Returns the canonical region for the given city, or {@link #UNKNOWN} when the
     * city is {@code null} or not present in the table.
     */
    public static String fromCity(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_TO_REGION.getOrDefault(city, UNKNOWN);
    }
}
