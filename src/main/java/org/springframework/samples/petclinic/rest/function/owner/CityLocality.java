package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region) from the city using a fixed
 * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD). Any city not
 * in the table derives {@code "UNKNOWN"}.
 */
public final class CityLocality {

    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CityLocality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
