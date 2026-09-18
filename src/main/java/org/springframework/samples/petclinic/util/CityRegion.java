package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Fixed city-to-region lookup. Maps a small set of known cities to their canonical
 * region (locality) code; every other city resolves to {@link #UNKNOWN}.
 */
public final class CityRegion {

    /** Region returned for any city not present in the {@link #TABLE table}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** The pinned city-to-region table. */
    private static final Map<String, String> TABLE = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private CityRegion() {
    }

    /**
     * The canonical region for the given city, or {@link #UNKNOWN} when the city is not in
     * the table (including a null city).
     */
    public static String locality(String city) {
        return city == null ? UNKNOWN : TABLE.getOrDefault(city, UNKNOWN);
    }

    /**
     * Whether {@code region} is a known canonical region (one of the {@link #TABLE table}'s
     * regions: NSW, VIC or QLD) rather than {@link #UNKNOWN} or absent.
     */
    public static boolean isKnown(String region) {
        return TABLE.containsValue(region);
    }
}
