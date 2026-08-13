package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region) from the city using a fixed
 * city-to-region table. Any city not in the table maps to {@link #UNKNOWN}.
 */
public final class Localities {

    /** Value returned when the city is not in the fixed table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private Localities() {
    }

    /** The canonical region for {@code city}, or {@code UNKNOWN} when not in the table. */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
