package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Fixed city-to-region lookup for the owner's derived 'locality'. The table is pinned:
 * Sydney -> NSW, Melbourne -> VIC, Brisbane -> QLD. Any other (or missing) city derives
 * the canonical region "UNKNOWN".
 */
public final class Localities {

    private static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private Localities() {
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
