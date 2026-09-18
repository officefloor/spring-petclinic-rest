package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Fixed city-to-region lookup used to derive an owner's locality. Cities not listed
 * resolve to {@link #UNKNOWN}.
 */
public final class CityRegion {

    /** The canonical region returned for any city not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_TO_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private CityRegion() {
    }

    /** The canonical region for {@code city}, or {@link #UNKNOWN} when it is not in the table. */
    public static String of(String city) {
        return CITY_TO_REGION.getOrDefault(city, UNKNOWN);
    }
}
