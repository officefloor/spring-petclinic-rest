package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (canonical region) from the city using a fixed
 * city-to-region table. Kept out of {@link OwnerMapper} so MapStruct does not mistake it
 * for a {@code String -> String} mapping method and apply it to every string property.
 */
public final class OwnerLocality {

    /** Fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocality() {
    }

    /**
     * Return the canonical region for {@code city}, or {@code "UNKNOWN"} when the city is
     * null or not present in the table.
     */
    public static String forCity(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
