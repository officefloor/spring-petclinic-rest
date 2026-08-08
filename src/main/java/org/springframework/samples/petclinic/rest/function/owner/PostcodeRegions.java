package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Fixed city-to-region and region-to-postcode-range tables used to validate an owner's
 * postcode against the region derived from the owner's city.
 *
 * <p>Cities: Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD. A city with no known
 * region accepts any 4-digit postcode. Ranges (inclusive): NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099.
 */
final class PostcodeRegions {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private PostcodeRegions() {
    }

    /** Canonical region for the city, or null when the city has no known region. */
    static String regionFor(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * True when the 4-digit postcode is valid for the city: valid when the city has no
     * known region, otherwise valid only when the postcode falls within that region's
     * inclusive range.
     */
    static boolean isValid(String city, int postcode) {
        String region = regionFor(city);
        if (region == null) {
            return true; // city with no known region accepts any 4-digit postcode
        }
        int[] range = REGION_RANGE.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }
}
