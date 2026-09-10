package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Fixed city-to-region lookup used to derive an owner's locality. Cities not in the
 * table derive the locality {@link #UNKNOWN}.
 */
public final class Localities {

    /** The canonical region string returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private Localities() {
    }

    /**
     * Derive the canonical region for a city, or {@link #UNKNOWN} when the city is not
     * in the fixed table (including when {@code city} is null).
     */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
