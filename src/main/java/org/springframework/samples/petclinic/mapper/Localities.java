package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (region) from its city using a fixed
 * city-to-region table. Any city not in the table maps to {@code UNKNOWN}.
 */
public final class Localities {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private Localities() {
    }

    /** The canonical region for the given city, or {@code UNKNOWN} when unlisted. */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
