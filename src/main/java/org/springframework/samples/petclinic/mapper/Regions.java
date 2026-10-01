package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * City-to-region lookup backing the {@code locality} field produced by
 * {@link OwnerMapper#toOwnerDto}. The table pins each known city to its canonical region string; any
 * city not listed derives the locality {@code "UNKNOWN"}.
 */
public final class Regions {

    /** City -> canonical region. Anything not listed derives locality {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** The locality returned for a city absent from {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Regions() {
    }

    /**
     * Derive the locality (canonical region) for a city.
     *
     * @param city the owner's city; may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String localityOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
