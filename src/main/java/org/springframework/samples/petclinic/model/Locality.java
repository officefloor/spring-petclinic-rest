package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Pinned locality reference data. A region is derived by postcode range first, falling back
 * to the city-to-region table; anything that matches neither resolves to {@link #UNKNOWN}.
 */
public final class Locality {

    /** The locality returned for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** A well-formed 4-digit postcode. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

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
     * Derive the canonical region, preferring the postcode. The postcode's range decides the
     * region when it falls in a known range; otherwise (postcode absent or unrecognised) the
     * city-to-region table decides.
     *
     * @param city     the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null} or non-numeric)
     * @return the canonical region string, or {@link #UNKNOWN} when neither postcode nor city is known
     */
    public static String regionFor(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : regionFor(city);
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
     * The region whose pinned postcode range contains the given postcode.
     *
     * @param postcode the owner's postcode (may be {@code null} or non-numeric)
     * @return the matching region, or {@code null} when the postcode is absent, non-numeric or in no range
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
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
