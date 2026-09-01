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
}
