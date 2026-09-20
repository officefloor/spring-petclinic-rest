package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * The pinned city-to-region table used to derive an owner's locality. Maps a small,
 * fixed set of cities to their canonical region string; any city not listed resolves to
 * {@link #UNKNOWN}.
 */
public final class CityRegion {

    /** Locality returned for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_TO_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private CityRegion() {
    }

    /**
     * The canonical region for the given city, or {@link #UNKNOWN} when the city is not
     * in the table (including a {@code null} city).
     */
    public static String localityOf(String city) {
        return CITY_TO_REGION.getOrDefault(city, UNKNOWN);
    }
}
