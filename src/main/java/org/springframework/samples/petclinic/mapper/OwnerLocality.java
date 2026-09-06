package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's locality (region) from their city using a fixed
 * city-to-region table. Kept as a plain static helper - rather than a method on
 * {@link OwnerMapper} - so MapStruct does not mistake it for an implicit
 * String-to-String property mapping and apply it to unrelated fields.
 */
final class OwnerLocality {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocality() {
    }

    /**
     * Returns the canonical region for the given city, or "UNKNOWN" when the city
     * is not in the fixed table (or is {@code null}).
     *
     * @param city the owner's city
     * @return the derived locality (region)
     */
    static String forCity(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

}
