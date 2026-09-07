package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Locality (canonical region) for pet owners, derived from the owner's city using a fixed
 * city-to-region table: {@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}.
 * Any city not in the table (or a null city) yields {@code UNKNOWN}. Derived purely from the
 * owner's own state, so it carries no stored data and is seed-independent. Used by the owner
 * mapper to expose {@code locality} on responses.
 */
public final class Locality {

    /** City -> canonical region. Fixed, pinned reference data. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /**
     * The canonical region for the given city, or {@code UNKNOWN} when the city is not in the
     * fixed table.
     */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
