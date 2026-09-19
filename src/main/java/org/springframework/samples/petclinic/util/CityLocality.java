package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Resolves an owner's locality (the canonical region). The postcode range is preferred
 * ({@link Postcodes#regionForPostcode(String)}); only when the postcode is absent or in
 * no known range does resolution fall back to a fixed city-to-region table:
 * Sydney->NSW, Melbourne->VIC, Brisbane->QLD.
 *
 * <p>Cities not in the table have no known region and resolve to {@code "UNKNOWN"}.
 */
public final class CityLocality {

    /** City -> canonical region. Anything not listed resolves to {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** The locality returned for a city with no known region. */
    public static final String UNKNOWN = "UNKNOWN";

    private CityLocality() {
    }

    /**
     * @param city the owner's city, as stored
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when it is
     *         not in the table (including a null city)
     */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Resolves the locality preferring the postcode range, disambiguating cities that
     * share a name. Falls back to {@link #forCity(String)} when the postcode is absent
     * or in no known range.
     *
     * @param postcode the owner's postcode, as stored (may be null)
     * @param city     the owner's city, as stored
     * @return the region derived from the postcode, else from the city, else
     *         {@code "UNKNOWN"}
     */
    public static String forPostcodeOrCity(String postcode, String city) {
        String region = Postcodes.regionForPostcode(postcode);
        return region != null ? region : forCity(city);
    }
}
