package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Pinned city-to-region reference data. Maps a known city to its canonical region string;
 * any city not in the table resolves to {@link #UNKNOWN}.
 */
public final class Locality {

    /** The locality returned for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    /**
     * Derive the canonical region for the given city.
     *
     * @param city the owner's city (may be {@code null})
     * @return the canonical region string, or {@link #UNKNOWN} when the city is not in the table
     */
    public static String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Whether the given 4-digit postcode is permitted for the given city. A city whose
     * region has a pinned postcode range accepts only postcodes within that (inclusive)
     * range; a city with no known region accepts any postcode.
     *
     * @param city     the owner's city (may be {@code null})
     * @param postcode the numeric value of a 4-digit postcode
     * @return {@code true} when the postcode is allowed for the city's region
     */
    public static boolean postcodeAllowedForCity(String city, int postcode) {
        int[] range = REGION_POSTCODES.get(regionFor(city));
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }
}
