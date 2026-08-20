package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Derives an owner's locality (region). The postcode is preferred: when it falls within a known
 * region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) that region is returned.
 * Only when the postcode is absent or in no known range does the fixed city-to-region table apply.
 * Anything the postcode and city both fail to resolve derives the locality {@code "UNKNOWN"}.
 */
public final class Localities {

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

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Locality returned for any city not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * @param city the owner's city (may be {@code null}).
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when it is not
     *         in the table.
     */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Derives the locality preferring the postcode over the city.
     *
     * @param postcode the owner's postcode (may be {@code null} or blank).
     * @param city     the owner's city (may be {@code null}).
     * @return the region for {@code postcode} when it falls in a known range; otherwise the
     *         city-derived region, or {@code "UNKNOWN"} when neither resolves.
     */
    public static String region(String postcode, String city) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : region(city);
    }

    /**
     * @return the region whose inclusive range contains {@code postcode}, or {@code null} when the
     *         postcode is absent, malformed, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || postcode.isBlank() || !FOUR_DIGITS.matcher(postcode).matches()) {
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
}
