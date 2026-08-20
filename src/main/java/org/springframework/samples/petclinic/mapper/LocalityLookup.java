package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (canonical region) from its city using a fixed
 * city-to-region table.
 *
 * <p>Deliberately a plain utility class rather than a method on {@link OwnerMapper}:
 * a {@code String -> String} method declared on a MapStruct {@code @Mapper} interface
 * would be picked up as a general property-conversion method and silently applied to
 * every string field. Kept here, it is only ever invoked from an explicit mapping
 * expression, so MapStruct never treats it as a conversion.
 */
public final class LocalityLookup {

    /** City -> canonical region; anything absent derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private LocalityLookup() {
    }

    /**
     * @param city the owner's city (may be {@code null})
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when it is not
     *         in the table
     */
    public static String regionFor(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
