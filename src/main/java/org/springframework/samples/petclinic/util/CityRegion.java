package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Pinned city-to-region reference table. An owner's locality is the canonical region of its city;
 * a city that is not in the table has region {@link #UNKNOWN}. Pure lookup with no dependency on
 * other owners, so it is derived at response time rather than stored on the entity.
 */
public final class CityRegion {

    /** The region reported for any city not in {@link #CITY_REGIONS}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGIONS = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private CityRegion() {
    }

    /**
     * The canonical region for {@code city}, or {@link #UNKNOWN} when the city is {@code null} or not
     * in the table.
     */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGIONS.getOrDefault(city, UNKNOWN);
    }
}
