package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Pinned locality reference data. A region is derived from a postcode range
 * ({@link #regionForPostcode}) or from the city-to-region table ({@link #regionFor(String)});
 * anything unrecognised resolves to {@link #UNKNOWN}.
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

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

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
     * Derive the canonical region from the postcode alone: the region whose pinned range
     * contains it.
     *
     * @param postcode the owner's postcode (may be {@code null} or non-numeric)
     * @return the matching region, or {@link #UNKNOWN} when the postcode is absent, non-numeric or in no range
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return UNKNOWN;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return UNKNOWN;
    }

    /**
     * Whether the given region is one of the pinned known regions (NSW, VIC or QLD).
     *
     * @param region the canonical region (may be {@code null} or {@link #UNKNOWN})
     * @return {@code true} when the region is in the pinned reference data
     */
    public static boolean isKnownRegion(String region) {
        return REGION_POSTCODES.containsKey(region);
    }

    /**
     * The IANA timezone name for the given region, via the pinned region-to-timezone table.
     *
     * @param region the canonical region (e.g. {@code "NSW"}, may be {@code null})
     * @return the IANA timezone name, or {@code null} when the region is not in the table
     */
    public static String timezoneForRegion(String region) {
        return REGION_TIMEZONE.get(region);
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
