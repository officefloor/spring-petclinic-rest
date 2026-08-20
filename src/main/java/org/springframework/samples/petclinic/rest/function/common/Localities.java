package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's locality (region) from their city using a fixed city-to-region table.
 * Any city not in the table derives the locality {@code "UNKNOWN"}.
 */
public final class Localities {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

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
}
