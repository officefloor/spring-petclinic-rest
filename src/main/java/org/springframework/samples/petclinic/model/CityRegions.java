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

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private CityRegions() {
    }

    /** The city's canonical region, or {@code null} when the city has no known region. */
    public static String regionOf(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The region's inclusive 4-digit postcode range {@code {low, high}}, or {@code null} when the
     * region has no known range. The returned array is a fresh copy, so callers cannot mutate the
     * shared table.
     */
    public static int[] postcodeRange(String region) {
        int[] range = REGION_POSTCODES.get(region);
        return range == null ? null : range.clone();
    }

    /**
     * The region whose postcode range contains {@code postcode}, or {@code null} when the
     * postcode is absent, not numeric, or in no known range.
     */
    public static String regionByPostcode(String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's locality, preferring the postcode: the region whose range contains the
     * postcode wins, falling back to the city-to-region table when the postcode is absent or
     * in no known range, and finally {@link #UNKNOWN} when the city has no known region.
     */
    public static String localityOf(String city, String postcode) {
        String region = regionByPostcode(postcode);
        if (region != null) {
            return region;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
