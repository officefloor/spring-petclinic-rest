package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Resolves an owner's locality (canonical region) from their city using a fixed
 * city-to-region table. Cities that are not in the table resolve to {@link #UNKNOWN}.
 */
public final class CityRegions {

    /** Locality returned for any city not present in {@link #CITY_TO_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Fixed, canonical city-to-region mapping. */
    private static final Map<String, String> CITY_TO_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private CityRegions() {
    }

    /**
     * Return the canonical region for {@code city}, or {@link #UNKNOWN} when the city is
     * absent from the table.
     */
    public static String localityOf(String city) {
        return CITY_TO_REGION.getOrDefault(city, UNKNOWN);
    }
}
