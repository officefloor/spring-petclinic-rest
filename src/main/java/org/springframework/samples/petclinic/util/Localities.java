package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality') from their city using a fixed
 * city-to-region table. Cities not in the table derive the locality {@code "UNKNOWN"}.
 */
public final class Localities {

    /** Fixed city -> canonical region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** The locality returned for any city not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * @param city the owner's city (may be {@code null}).
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     *         city is {@code null} or not in the fixed table.
     */
    public static String localityFor(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
