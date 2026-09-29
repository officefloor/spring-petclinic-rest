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

    /**
     * Fixed version tag mixed into the version-2 owner identity. It is baked <em>inside</em> the
     * derived identifiers (the region code prefixing the {@code memberId}, and the hashed
     * {@code householdId} and {@code identityKey}) so every version-2 value differs from the value
     * version 1 produced. It is deliberately kept out of the user-facing {@code locality},
     * {@code timezone} and the owner segment's derived region, which stay the plain region code.
     */
    public static final String IDENTITY_VERSION_TAG = "V2";

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
     * Whether {@code region} is one of the known regions (NSW, VIC or QLD). {@code false} for
     * {@link #UNKNOWN}, {@code null}, or any other value.
     */
    public static boolean isKnownRegion(String region) {
        return REGION_POSTCODES.containsKey(region);
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
     * known range. This is the {@code <REGION>} component of an owner's {@code memberId}.
     */
    public static String regionForPostcode(String postcode) {
        String region = forPostcode(postcode);
        return region == null ? UNKNOWN : region;
    }

    /**
     * The plain {@code <REGION>} code carried by a version-2 {@code memberId} formatted
     * {@code '<V2><REGION><FY><HASH8><CHK>'} — one of the known regions (NSW, VIC, QLD) when the
     * memberId carries it after the {@link #IDENTITY_VERSION_TAG} prefix, otherwise {@link #UNKNOWN}
     * (which also covers a memberId that is {@code null}, blank, or carries the literal
     * {@code UNKNOWN} region). The leading version tag is stripped before matching, so the returned
     * region is always the plain code and never carries the {@code V2} tag. The matching layout is
     * produced by {@code ClinicServiceImpl.generateMemberId} and also read by
     * {@link #fiscalYearSegmentOf(String)}; keep the three in sync.
     */
    public static String regionOfMemberId(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return UNKNOWN;
        }
        String body = memberIdBody(memberId);
        for (String region : REGION_POSTCODES.keySet()) {
            if (body.startsWith(region)) {
                return region;
            }
        }
        return UNKNOWN;
    }

    /**
     * The two-character fiscal-year (FY) segment of a version-2 {@code memberId} formatted
     * {@code '<V2><REGION><FY><HASH8><CHK>'} — the two characters following the
     * {@link #IDENTITY_VERSION_TAG} prefix and the {@code <REGION>} code (see
     * {@link #regionOfMemberId(String)}) — or {@code null} when the memberId is {@code null}, blank,
     * or too short to carry an FY segment. This keeps the {@code <V2><REGION><FY>} layout parsing in
     * one place; callers (e.g. {@code Owner.getFiscalYear}) format it rather than re-deriving the
     * tag/region offsets themselves.
     */
    public static String fiscalYearSegmentOf(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return null;
        }
        String body = memberIdBody(memberId);
        int start = regionOfMemberId(memberId).length();
        if (body.length() < start + 2) {
            return null;
        }
        return body.substring(start, start + 2);
    }

    /**
     * The {@code memberId} with its leading {@link #IDENTITY_VERSION_TAG} version tag stripped (when
     * present), leaving the {@code <REGION><FY><HASH8><CHK>} body. The single place that knows the
     * memberId carries the tag as a prefix.
     */
    private static String memberIdBody(String memberId) {
        return memberId.startsWith(IDENTITY_VERSION_TAG)
            ? memberId.substring(IDENTITY_VERSION_TAG.length()) : memberId;
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
