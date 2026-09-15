package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Map;

/**
 * Fixed lookup from an owner's city to its canonical region (its "locality"). The table
 * is pinned; any city not listed has locality {@link #UNKNOWN}.
 */
public final class CityRegion {

    /** The locality returned for any city not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City (matched case-insensitively) to canonical region string. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "sydney", "NSW",
        "melbourne", "VIC",
        "brisbane", "QLD");

    private CityRegion() {
    }

    /**
     * The canonical region for the given city, or {@link #UNKNOWN} when the city is not
     * in the table. Matching is case-insensitive; a null city is {@link #UNKNOWN}.
     */
    public static String localityOf(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city.toLowerCase(Locale.ROOT), UNKNOWN);
    }
}
