package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's canonical region ("locality") from their city using a fixed
 * city-to-region table. The mapping is intentionally pinned so both the Spring and
 * OfficeFloor arms produce identical values; anything not in the table is "UNKNOWN".
 */
public final class Localities {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Locality returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Region -> IANA timezone name. Pinned to match the acceptance spec's fixed table; regions absent
     *  here (UNKNOWN) have no timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    /** Region -> inclusive 4-digit postcode range {low, high}. Regions absent here (UNKNOWN) accept
     *  any 4-digit postcode. Pinned to match the acceptance spec's fixed table. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Localities() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when the city is not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region derived from the {@code postcode} alone — the region whose range contains
     * it, or "UNKNOWN" when the postcode is absent, blank, non-numeric or in no known range. This is
     * the region embedded in an owner's {@code customerCode}.
     */
    public static String ofPostcode(String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : UNKNOWN;
    }

    /** The region whose inclusive range contains {@code postcode}, or {@code null} when the postcode is
     *  absent, blank, non-numeric or in no known range. */
    private static String regionForPostcode(String postcode) {
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

    /** The IANA timezone name for {@code region}, or {@code null} when the region has no known
     *  timezone (e.g. "UNKNOWN"). */
    public static String timezoneOfRegion(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /** The inclusive {low, high} postcode range for {@code city}'s region, or {@code null} when the
     *  city has no known region (any 4-digit postcode is then accepted). */
    public static int[] postcodeRange(String city) {
        return REGION_POSTCODES.get(of(city));
    }
}
