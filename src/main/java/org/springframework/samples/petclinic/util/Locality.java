package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's {@code locality}: the canonical region for the owner's city,
 * from a fixed city-to-region table. Any city not in the table resolves to
 * {@code "UNKNOWN"}.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not mapped. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
