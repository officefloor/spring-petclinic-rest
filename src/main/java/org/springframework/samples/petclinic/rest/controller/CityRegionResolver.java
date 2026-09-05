/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Resolves an owner's city to its canonical region using a single fixed table.
 *
 * <p>Keeping this in one place means every rule that is keyed by an owner's region reads
 * the same city-to-region mapping, so they can never drift apart: the region a city
 * derives here is the region every region-keyed rule sees for that city.
 *
 * <p>A city not listed in the table has no known region and resolves to
 * {@link #UNKNOWN_REGION}.
 */
@Component
public class CityRegionResolver {

    /** The region returned for a city that is not in the fixed {@link #CITY_REGION} table. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    /** Fixed city-to-region table; a city not listed here has no known region. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Fixed region-to-postcode range table (inclusive 4-digit low/high), keyed by region; a
     *  region not listed here (i.e. {@link #UNKNOWN_REGION}) accepts any 4-digit postcode. */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Fixed region-to-timezone table (IANA names), keyed by region; a region not listed here
     *  has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Resolve a city to its canonical region.
     *
     * @param city the owner's city, as stored
     * @return the region the city belongs to, or {@link #UNKNOWN_REGION} when the city is
     *         not in the fixed table
     */
    public String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN_REGION);
    }

    /**
     * Resolve a region preferring the postcode over the city. The {@code postcode} is looked up
     * against the region postcode ranges first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); only
     * when the postcode is absent, non-numeric or in no known range does this fall back to the
     * city-to-region table. For a known city with a matching postcode both agree; a postcode in a
     * different region's range disambiguates cities that share a name.
     *
     * @param city     the owner's city, as stored
     * @param postcode a 4-digit postcode string (e.g. '3000'), or {@code null} when absent
     * @return the region derived from the postcode, else the region derived from the city, else
     *         {@link #UNKNOWN_REGION}
     */
    public String regionFor(String city, String postcode) {
        String region = regionForPostcode(postcode);
        return region != null ? region : regionFor(city);
    }

    /**
     * The region code stamped INSIDE an owner's derived identifiers (the REGION segment of its
     * memberId, and the region any other identity code mixes in), resolved preferring the postcode
     * over the city exactly as {@link #regionFor(String, String)}.
     *
     * <p>This is a deliberately separate seam from the user-facing locality region: today both
     * resolve to the same canonical region, but keeping the identifier's region derivation in its
     * own place means the value woven into identifiers can evolve without disturbing the plain
     * region reported as an owner's locality, timezone or segment area.
     *
     * @param city     the owner's city, as stored
     * @param postcode a 4-digit postcode string (e.g. '2000'), or {@code null} when absent
     * @return the region code to embed in the owner's identifiers
     */
    public String identityRegionFor(String city, String postcode) {
        return regionFor(city, postcode);
    }

    /**
     * The IANA timezone name for a region, using the fixed region-to-timezone table
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane).
     *
     * @param region the canonical region code (e.g. 'NSW'), such as returned by {@link #regionFor}
     * @return the region's IANA timezone name, or {@code null} when the region is not in the table
     */
    public String timezoneForRegion(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /**
     * The region whose postcode range contains {@code postcode}, or {@code null} when the postcode
     * is absent, non-numeric or in no known range.
     */
    private String regionForPostcode(String postcode) {
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
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether a 4-digit {@code postcode} is valid for the given {@code city}. The postcode is
     * checked against the inclusive range of the city's region (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099); a city with no known region accepts any 4-digit postcode.
     *
     * @param city     the owner's city, as stored
     * @param postcode a 4-digit postcode string (e.g. '2000')
     * @return {@code true} if the postcode is within the city's region range, or the city has
     *         no known region; {@code false} if the postcode is out of range for the region
     */
    public boolean isPostcodeValidForCity(String city, String postcode) {
        int[] range = REGION_POSTCODE_RANGE.get(regionFor(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
