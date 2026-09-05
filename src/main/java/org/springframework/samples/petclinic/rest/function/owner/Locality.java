package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives an owner's {@code locality}: the canonical region for the owner's city, looked up in a
 * fixed city-to-region table (Sydney -> NSW, Melbourne -> VIC, Brisbane -> QLD). Any city not in the
 * table (including a null or blank city) derives {@code "UNKNOWN"}. The mapping is pinned so it is
 * stable across requests and needs no persisted column.
 */
public final class Locality {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
