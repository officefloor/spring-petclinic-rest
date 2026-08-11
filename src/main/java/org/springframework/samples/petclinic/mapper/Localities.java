package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ("locality"). The postcode is preferred:
 * a 4-digit postcode is matched against each region's inclusive range first, and
 * only when the postcode is absent or in no known range does derivation fall back
 * to a fixed city-to-region table. Kept as a plain utility (rather than a mapper
 * method) so MapStruct does not treat it as an implicit {@code String -> String}
 * mapping.
 */
public final class Localities {

    /** City -> canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region returned for a city that is absent from {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * Returns the canonical region for {@code city} from the fixed table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}),
     * or {@code "UNKNOWN"} when the city is {@code null} or not in the table.
     *
     * @param city the owner's city
     * @return the canonical region string, or {@code "UNKNOWN"} when unknown
     */
    public static String forCity(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Returns the canonical region, preferring the {@code postcode}: it is matched
     * against each region's inclusive postcode range first (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). Only when the postcode is {@code null},
     * non-numeric, or in no known range does derivation fall back to the
     * {@link #forCity(String) city-to-region table}. This returns the same region
     * for known cities but disambiguates cities that share a name.
     *
     * @param city the owner's city
     * @param postcode the owner's postcode, may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when unknown
     */
    public static String forCityAndPostcode(String city, String postcode) {
        String region = forPostcode(postcode);
        if (region != null) {
            return region;
        }
        return forCity(city);
    }

    /**
     * Returns the region whose inclusive postcode range contains {@code postcode},
     * or {@code null} when the postcode is {@code null}, non-numeric, or in no
     * known range.
     */
    private static String forPostcode(String postcode) {
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
