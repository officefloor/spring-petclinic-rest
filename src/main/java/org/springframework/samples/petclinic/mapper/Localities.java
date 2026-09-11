package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (region) preferring the postcode over the city.
 *
 * <p>The region is resolved by postcode range first (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099); only when the postcode is absent or in no known range does it fall
 * back to a fixed city-to-region table. Anything unresolved maps to {@code UNKNOWN}.
 * This yields the same region for known cities but disambiguates cities that share a
 * name.
 */
public final class Localities {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Localities() {
    }

    /** The canonical region for the given city, or {@code UNKNOWN} when unlisted. */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The region whose postcode range contains the given 4-digit postcode, or
     * {@code null} when the postcode is absent, malformed, or in no known range.
     */
    public static String forPostcode(String postcode) {
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
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The region for an owner, preferring the postcode range and falling back to the
     * city-to-region table when the postcode is absent or in no known range.
     */
    public static String forCityAndPostcode(String city, String postcode) {
        String byPostcode = forPostcode(postcode);
        return byPostcode != null ? byPostcode : forCity(city);
    }
}
