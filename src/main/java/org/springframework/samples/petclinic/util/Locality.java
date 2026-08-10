package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives the canonical region ('locality') for an owner's city using a fixed
 * city-to-region table. Cities not in the table derive the locality 'UNKNOWN'.
 */
public final class Locality {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code UNKNOWN} when not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
