package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * Single source of truth for the fixed city-to-region table and the per-region postcode ranges.
 *
 * <p>This is a domain concept shared by the read-time {@code locality} derivation (see
 * {@code org.springframework.samples.petclinic.mapper.OwnerMapper}) and the create/update postcode
 * validation in the REST pipeline, so it lives in the business layer that both already depend on
 * rather than in either. A city not in the table has region {@link #UNKNOWN} and accepts any 4-digit
 * postcode.
 */
public final class OwnerRegion {

    /** Region reported for a city that is not in the fixed table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** Fixed city-to-region table; any other city has region {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private OwnerRegion() {
    }

    /** The canonical region for {@code city}, or {@link #UNKNOWN} when the city is not in the table. */
    public static String regionOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The locality for an owner, preferring the postcode: when {@code postcode} is a 4-digit value
     * inside a known region's range that region wins, disambiguating cities that share a name.
     * Otherwise (postcode absent, malformed, or in no known range) this falls back to the
     * city-to-region table, which still returns {@link #UNKNOWN} for an unknown city.
     */
    public static String localityOf(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : regionOf(city);
    }

    /** The region whose inclusive range contains {@code postcode}, or {@code null} when none does. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether {@code postcode} is acceptable for {@code city}. A postcode must be exactly four
     * digits; a city whose region is known must additionally fall within that region's inclusive
     * range, while a city with no known region accepts any 4-digit postcode.
     */
    public static boolean isPostcodeValid(String city, String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(regionOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
