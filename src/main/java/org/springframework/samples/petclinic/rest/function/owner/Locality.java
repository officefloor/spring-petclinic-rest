package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single definition of an owner's locality: the canonical region derived from its city
 * via a fixed city-to-region table. Cities not in the table resolve to {@code "UNKNOWN"}.
 *
 * <p>Lookup only; a pure function of the city, so the region is computed on read rather than
 * stored.
 */
public final class Locality {

    /** The unknown-region marker returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not in the table. */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
