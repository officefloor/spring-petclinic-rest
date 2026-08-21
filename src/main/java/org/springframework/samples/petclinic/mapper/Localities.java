package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical locality (region) from their city using a fixed
 * city-to-region table. Kept as a plain static helper (not a mapper method) so
 * MapStruct does not treat it as an implicit String-to-String mapping method.
 */
public final class Localities {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Localities() {
    }

    /** The canonical region for the given city, or "UNKNOWN" when the city is not in the table. */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
