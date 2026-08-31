package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/** Derives an owner's locality (canonical region) from its city using a fixed table. */
public final class Locality {

    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
