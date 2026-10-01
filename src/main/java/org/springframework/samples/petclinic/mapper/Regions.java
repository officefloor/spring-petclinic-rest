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

    /** Region -> inclusive 4-digit postcode range {low, high}. A region absent from this table (and
     *  the {@link #UNKNOWN} locality) imposes no postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** The locality returned for a city absent from {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Regions() {
    }

    /**
     * Whether a 4-digit postcode is valid for the given city. A city whose region has no pinned
     * postcode range (including an unknown city) accepts any value.
     *
     * @param city     the owner's city; may be {@code null}
     * @param postcode the already-4-digit postcode to check
     * @return {@code true} when the city's region imposes no range or the postcode falls within it
     */
    public static boolean postcodeMatchesCity(String city, String postcode) {
        int[] range = REGION_POSTCODES.get(localityOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
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
