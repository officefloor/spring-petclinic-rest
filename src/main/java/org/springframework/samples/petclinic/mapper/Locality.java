package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality') from their city using a fixed
 * city-to-region table.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for an implicit {@code String -> String} mapping method and apply it to
 * every string property.
 */
public final class Locality {

    /** Fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not in the table. */
    public static String of(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
