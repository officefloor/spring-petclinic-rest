package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's canonical region ("locality") from their city using a fixed
 * city-to-region table. The mapping is intentionally pinned so both the Spring and
 * OfficeFloor arms produce identical values; anything not in the table is "UNKNOWN".
 */
public final class Localities {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Locality returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when the city is not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
