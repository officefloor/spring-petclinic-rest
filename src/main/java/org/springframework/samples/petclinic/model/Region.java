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
 * Single source of truth for the fixed city-to-region mapping (Sydney->NSW, Melbourne->VIC,
 * Brisbane->QLD). Consumed by presentation (an owner's {@code locality}) and by validation (an
 * owner's postcode range), so both layers agree on which region a city belongs to.
 */
public final class Region {

    /** Locality reported for a city that has no known region. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Fixed city-to-region lookup. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Fixed region-to-timezone lookup, as IANA names. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private Region() {
    }

    /**
     * The canonical region for {@code city}, or {@code null} when the city has no known region.
     */
    public static String forCity(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The canonical region whose range contains the given 4-digit {@code postcode}, or {@code null}
     * when the postcode is absent, not a number, or falls in no known range.
     */
    public static String forPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException e) {
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
     * The inclusive 4-digit postcode range {@code {low, high}} for {@code region}, or {@code null}
     * when the region has no defined range. Returns a fresh array so callers cannot mutate the table.
     */
    public static int[] postcodeRange(String region) {
        int[] range = REGION_POSTCODES.get(region);
        return range == null ? null : new int[] {range[0], range[1]};
    }

    /**
     * The canonical region for {@code city}, or {@link #UNKNOWN} when the city has no known region.
     */
    public static String localityForCity(String city) {
        String region = forCity(city);
        return region == null ? UNKNOWN : region;
    }

    /**
     * The canonical region derived from the given 4-digit {@code postcode} (see
     * {@link #forPostcode(String)}), or {@link #UNKNOWN} when the postcode is absent or falls in no
     * known range. This is the {@code <REGION>} component of an owner's {@code customerCode}.
     */
    public static String regionForPostcode(String postcode) {
        String region = forPostcode(postcode);
        return region == null ? UNKNOWN : region;
    }

    /**
     * The {@code <REGION>} component of a {@code customerCode} formatted {@code '<REGION>-<HASH8>'}
     * — the text before the first {@code '-'} — or {@link #UNKNOWN} when {@code customerCode} is
     * {@code null}, blank, or carries no separator. The matching layout is produced by
     * {@code ClinicServiceImpl.generateCustomerCode}; keep the two in sync.
     */
    public static String regionOfCustomerCode(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            return UNKNOWN;
        }
        int dash = customerCode.indexOf('-');
        return dash <= 0 ? UNKNOWN : customerCode.substring(0, dash);
    }

    /**
     * The IANA timezone name for {@code region} via the fixed region-to-timezone table
     * (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane), or {@code null}
     * when the region has no mapped timezone (e.g. {@link #UNKNOWN} or {@code null}).
     */
    public static String timezoneForRegion(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /**
     * The canonical region derived by preferring the {@code postcode} range, and only falling back
     * to the city-to-region table when the postcode is absent or in no known range. Returns
     * {@link #UNKNOWN} when neither the postcode nor the city yields a region.
     */
    public static String locality(String city, String postcode) {
        String region = forPostcode(postcode);
        if (region == null) {
            region = forCity(city);
        }
        return region == null ? UNKNOWN : region;
    }
}
