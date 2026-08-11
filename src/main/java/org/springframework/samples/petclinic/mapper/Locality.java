package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region) from its city using a fixed
 * city-to-region table. Kept out of {@link OwnerMapper} so MapStruct does not
 * mistake the {@code String -> String} helper for an implicit mapping method and
 * apply it to every string property.
 */
public final class Locality {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
