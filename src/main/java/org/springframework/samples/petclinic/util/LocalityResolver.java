package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (region) from its city using a fixed
 * city-to-region table. Cities not in the table derive the locality
 * {@code "UNKNOWN"}.
 */
public final class LocalityResolver {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** The locality returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     * city (including {@code null}) is not in the table.
     */
    public static String localityOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
