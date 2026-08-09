package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's locality (region) from their city using a fixed city-to-region table.
 *
 * <p>The table pins Sydney -&gt; NSW, Melbourne -&gt; VIC and Brisbane -&gt; QLD; any city not
 * listed derives the locality {@code "UNKNOWN"}.
 */
public final class LocalityLookup {

    /** City -&gt; canonical region; anything not listed derives locality {@code "UNKNOWN"}. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private LocalityLookup() {
    }

    /**
     * Returns the canonical region for the given city, or {@code "UNKNOWN"} when the city is not in
     * the fixed city-to-region table (including a {@code null} city).
     *
     * @param city the owner's stored city, or {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
