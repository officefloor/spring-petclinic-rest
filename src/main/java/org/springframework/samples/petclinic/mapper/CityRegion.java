package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives the canonical region (locality) for a city from a fixed city-to-region table.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for a {@code String -> String} mapping method and apply it to unrelated fields.
 */
public final class CityRegion {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CityRegion() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when the city is not in the table. */
    public static String locality(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
