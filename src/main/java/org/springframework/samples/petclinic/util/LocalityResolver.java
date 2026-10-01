package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's 'locality' (canonical region) from the city using a fixed
 * city-to-region table.
 *
 * <p>The table is: {@code Sydney -> NSW}, {@code Melbourne -> VIC},
 * {@code Brisbane -> QLD}. Any city not listed (or a {@code null} city) resolves to
 * {@code 'UNKNOWN'}.
 */
public final class LocalityResolver {

    /** City -> canonical region. Cities not listed derive locality 'UNKNOWN'. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Locality returned when the city is not in the table. */
    private static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Resolve the canonical region for a city.
     *
     * @param city the owner's city (any value)
     * @return the canonical region string, or {@code 'UNKNOWN'} when the city is not
     *         in the table.
     */
    public static String resolve(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
