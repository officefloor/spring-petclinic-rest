package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Derives an owner's locality (region) from their city using a fixed
 * city-to-region table.
 */
public final class Locality {

    /** City -> canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Returned when the city is null or not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /**
     * The canonical region string for the given city, or {@link #UNKNOWN} when the
     * city is null or not in the fixed city-to-region table.
     */
    public static String of(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
