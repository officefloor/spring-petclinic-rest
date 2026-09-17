package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Pinned region-to-postcode-range reference table. Each canonical region (see {@link CityRegion})
 * permits an inclusive 4-digit postcode range; a region not in the table — including
 * {@link CityRegion#UNKNOWN} — accepts any 4-digit postcode (the permissive default). Pure lookup
 * with no dependency on other owners.
 */
public final class RegionPostcodes {

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private RegionPostcodes() {
    }

    /**
     * Whether {@code postcode} is allowed for {@code region}: within the region's inclusive range, or
     * always allowed when the region has no known range.
     */
    public static boolean allows(String region, int postcode) {
        int[] range = REGION_RANGES.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }

    /**
     * The canonical region whose inclusive range contains {@code postcode}, or {@code null} when no
     * known range does. The reverse of {@link #allows(String, int)}.
     */
    public static String regionOf(int postcode) {
        for (Map.Entry<String, int[]> entry : REGION_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (postcode >= range[0] && postcode <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
