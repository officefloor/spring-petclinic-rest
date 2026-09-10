package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Fixed city-to-region lookup used to derive an owner's locality. Cities not in the
 * table derive the locality {@link #UNKNOWN}.
 */
public final class Localities {

    /** The canonical region string returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Localities() {
    }

    /**
     * Derive the canonical region for a city, or {@link #UNKNOWN} when the city is not
     * in the fixed table (including when {@code city} is null).
     */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The inclusive {@code {low, high}} 4-digit postcode range for a region, or {@code null}
     * when the region is not in the fixed table (i.e. {@link #UNKNOWN}), meaning any 4-digit
     * postcode is acceptable.
     */
    public static int[] postcodeRange(String region) {
        return REGION_POSTCODES.get(region);
    }
}
