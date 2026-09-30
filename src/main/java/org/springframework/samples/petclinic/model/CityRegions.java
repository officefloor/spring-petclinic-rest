package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * The single definition of the city-to-region fact (Sydney-&gt;NSW, Melbourne-&gt;VIC,
 * Brisbane-&gt;QLD). Both the mapping layer (which derives an owner's {@code locality}) and the
 * request-handling layer (which validates a postcode against the city's region) depend on this
 * one table, so adding a city updates both behaviours together.
 *
 * <p>Lives beside {@link Owner} — the same home as {@link OwnerIdentity} — so both layers can
 * depend on it without a package cycle.
 */
public final class CityRegions {

    /** Region reported for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -&gt; canonical region; anything not listed has no known region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private CityRegions() {
    }

    /** The city's canonical region, or {@code null} when the city has no known region. */
    public static String regionOf(String city) {
        return CITY_REGION.get(city);
    }

    /** The city's canonical region, or {@link #UNKNOWN} when the city has no known region. */
    public static String localityOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
