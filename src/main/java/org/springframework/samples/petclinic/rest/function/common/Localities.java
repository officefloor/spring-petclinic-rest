package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Region lookup for the owner's derived 'locality'. The postcode is preferred: it is resolved
 * against the pinned ranges first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, see
 * {@link Postcodes}), and only when the postcode is absent or in no known range does the
 * lookup fall back to the pinned city-to-region table (Sydney -> NSW, Melbourne -> VIC,
 * Brisbane -> QLD). Any other (or missing) city derives the canonical region "UNKNOWN".
 */
public final class Localities {

    private static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    private Localities() {
    }

    /**
     * The canonical region for the owner, preferring the postcode. Resolves {@code postcode}
     * against the pinned ranges first and only falls back to {@code city} when the postcode is
     * absent or in no known range. This returns the same region for a known city but
     * disambiguates cities that share a name.
     */
    public static String of(String postcode, String city) {
        String byPostcode = Postcodes.regionOf(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
