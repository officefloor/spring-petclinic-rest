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
     * Derive the locality (canonical region) preferring the postcode. The postcode's region range is
     * consulted first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); only when the postcode is absent
     * or falls in no known range does the city-to-region table decide. This returns the same region
     * for known cities but disambiguates cities that share a name.
     *
     * @param city     the owner's city; may be {@code null}
     * @param postcode the owner's postcode; may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when neither postcode nor city resolves
     */
    public static String localityOf(String city, String postcode) {
        String byPostcode = regionOfPostcode(postcode);
        return byPostcode != null ? byPostcode : localityOf(city);
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

    /**
     * The region whose postcode range contains the given postcode, or {@code null} when the postcode
     * is absent, non-numeric, or falls in no known range.
     */
    private static String regionOfPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
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
}
