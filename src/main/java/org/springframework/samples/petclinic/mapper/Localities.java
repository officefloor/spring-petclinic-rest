package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's locality (region) from its city using a fixed
 * city-to-region table. Cities not in the table resolve to "UNKNOWN".
 */
final class Localities {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Localities() {
    }

    /** Canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    static String regionOf(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Canonical region preferring the postcode range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099); falls back to the city table when the postcode is absent or in
     * no known range.
     */
    static String regionOf(String city, String postcode) {
        String byPostcode = regionOfPostcode(postcode);
        return byPostcode != null ? byPostcode : regionOf(city);
    }

    /** Region for a postcode's range, or {@code null} when absent or out of range. */
    private static String regionOfPostcode(String postcode) {
        Integer code = parse(postcode);
        if (code == null) {
            return null;
        }
        if (code >= 2000 && code <= 2099) {
            return "NSW";
        }
        if (code >= 3000 && code <= 3099) {
            return "VIC";
        }
        if (code >= 4000 && code <= 4099) {
            return "QLD";
        }
        return null;
    }

    private static Integer parse(String postcode) {
        try {
            return postcode == null ? null : Integer.valueOf(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
