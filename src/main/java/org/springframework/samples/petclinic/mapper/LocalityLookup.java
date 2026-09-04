package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality') from its city using a fixed
 * city-to-region table. Cities absent from the table resolve to 'UNKNOWN'.
 */
public final class LocalityLookup {

    private static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private LocalityLookup() {
    }

    /** The canonical region for {@code city}, or 'UNKNOWN' when it is not in the table. */
    public static String regionOf(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
