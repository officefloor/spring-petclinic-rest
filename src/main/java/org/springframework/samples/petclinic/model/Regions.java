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
 * The fixed region lookup shared across the domain. It is the single source of truth for mapping an
 * owner to its canonical region: by postcode range first (NSW 2000&ndash;2099, VIC 3000&ndash;3099,
 * QLD 4000&ndash;4099) and, failing that, by the city-to-region table (Sydney&rarr;NSW,
 * Melbourne&rarr;VIC, Brisbane&rarr;QLD). Anything the tables do not cover resolves to
 * {@link #UNKNOWN}.
 */
public final class Regions {

    /** The region assigned to any owner not resolved by the postcode or city tables. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGIONS =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Regions() {
    }

    /**
     * Resolves the canonical region for an owner, preferring the postcode over the city: the region
     * whose range contains {@code postcode} is used, and only when the postcode is absent or in no
     * known range does the {@link #regionOfCity(String) city table} decide.
     *
     * @param postcode the owner's postcode, may be {@code null} or blank
     * @param city the owner's city
     * @return the canonical region, or {@link #UNKNOWN} when neither table matches
     */
    public static String localityOf(String postcode, String city) {
        String byPostcode = regionOfPostcode(postcode);
        return UNKNOWN.equals(byPostcode) ? regionOfCity(city) : byPostcode;
    }

    /**
     * @param city the city to resolve
     * @return the canonical region for {@code city}, or {@link #UNKNOWN} when the city is not in the
     * fixed table
     */
    public static String regionOfCity(String city) {
        return CITY_REGIONS.getOrDefault(city, UNKNOWN);
    }

    /**
     * @param postcode the postcode to resolve, may be {@code null} or blank
     * @return the region whose inclusive range contains {@code postcode}, or {@link #UNKNOWN} when
     * the postcode is absent, unparseable, or in no known range
     */
    public static String regionOfPostcode(String postcode) {
        Integer value = parse(postcode);
        if (value != null) {
            for (Map.Entry<String, int[]> entry : REGION_RANGES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * @param region a canonical region
     * @return that region's inclusive {low, high} postcode range, or {@code null} when the region has
     * no known range
     */
    public static int[] rangeOf(String region) {
        return REGION_RANGES.get(region);
    }

    private static Integer parse(String postcode) {
        if (postcode == null) {
            return null;
        }
        try {
            return Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
