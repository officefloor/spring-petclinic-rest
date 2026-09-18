package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Fixed region-to-postcode-range lookup used to validate an owner's postcode against the region
 * derived from its city (see {@link CityRegion}). A region not listed here — including
 * {@link CityRegion#UNKNOWN} — imposes no range constraint, so any 4-digit postcode is accepted.
 */
public final class RegionPostcodes {

    /** Region -> inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_TO_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private RegionPostcodes() {
    }

    /**
     * Whether {@code postcode} is permitted in {@code region}: {@code true} for any region without a
     * known range, otherwise only when it falls inside that region's inclusive range.
     */
    public static boolean isInRange(String region, int postcode) {
        int[] range = REGION_TO_RANGE.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }

    /**
     * The region whose range contains {@code postcode}, or {@code null} when {@code postcode} is
     * absent, not a 4-digit value, or in no known range. Used to derive locality from the postcode
     * ahead of the city.
     */
    public static String regionOf(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_TO_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
