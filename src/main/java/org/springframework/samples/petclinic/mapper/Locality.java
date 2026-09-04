package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives the owner's locality: the canonical region for the city from a fixed
 * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), or
 * {@code UNKNOWN} when the city is not in the table. The value is a pure function
 * of the city, so it needs no stored state.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
