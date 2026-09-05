package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives the {@code locality} (canonical region) from an owner's city using a fixed
 * city-to-region table. Cities not in the table resolve to {@code UNKNOWN}.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
