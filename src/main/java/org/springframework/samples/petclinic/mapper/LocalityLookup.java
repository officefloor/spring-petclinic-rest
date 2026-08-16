package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} from its city using a fixed city-to-region
 * table. Kept out of the mapper interface deliberately: MapStruct treats any
 * single-argument method on a {@code @Mapper} interface as a candidate mapping
 * method, so a {@code String -> String} helper there would be applied to every
 * String property. This static lookup is referenced only from an explicit
 * mapping expression instead.
 */
public final class LocalityLookup {

    /** City -> canonical region; an exact, case-sensitive mapping. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private LocalityLookup() {
    }

    /**
     * Returns the canonical region for {@code city} from the fixed table, or
     * {@code 'UNKNOWN'} when the city is not in the table.
     *
     * @param city the owner's city, possibly {@code null}
     * @return the canonical region, or {@code 'UNKNOWN'} if the city is unknown
     */
    public static String regionFor(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
