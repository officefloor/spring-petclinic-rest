package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Pinned city-to-region reference data. Maps a known city to its canonical region string;
 * any city not in the table resolves to {@link #UNKNOWN}.
 */
public final class Locality {

    /** The locality returned for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private Locality() {
    }

    /**
     * Derive the canonical region for the given city.
     *
     * @param city the owner's city (may be {@code null})
     * @return the canonical region string, or {@link #UNKNOWN} when the city is not in the table
     */
    public static String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
