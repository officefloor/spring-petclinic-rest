package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region) from its city.
 *
 * <p>The city is looked up in a small, fixed city-to-region table
 * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}) and the matching
 * canonical region string is returned. Any city not present in the table — including a
 * {@code null} city — derives the sentinel locality {@code "UNKNOWN"}.
 */
public final class LocalityResolver {

    /** Locality returned for any city not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region; anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private LocalityResolver() {
    }

    /**
     * Resolve the canonical region for a city.
     *
     * @param city the owner's city, possibly {@code null}
     * @return the canonical region string when the city is in the table, otherwise {@code "UNKNOWN"}
     */
    public static String resolve(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
