package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Maps a city to its canonical region using a fixed city-to-region table.
 */
public final class Locality {

    private static final Map<String, String> REGIONS = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private Locality() {
    }

    /**
     * Returns the canonical region for the given city, or {@code "UNKNOWN"} when the city
     * is not in the table.
     */
    public static String of(String city) {
        return REGIONS.getOrDefault(city, "UNKNOWN");
    }
}
