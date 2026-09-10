package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Fixed lookups used to derive an owner's locality. The locality prefers the postcode
 * range and falls back to the city-to-region table; cities not in the table (and with no
 * postcode-derived region) derive the locality {@link #UNKNOWN}.
 */
public final class Localities {

    /** The canonical region string returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private Localities() {
    }

    /**
     * Derive the canonical region for a city, or {@link #UNKNOWN} when the city is not
     * in the fixed table (including when {@code city} is null).
     */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Derive the canonical region, preferring the postcode. When {@code postcode} is a
     * 4-digit value falling in a known region's range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) that region is returned, disambiguating cities that share a name.
     * Otherwise (postcode absent, non-numeric, or in no known range) the fixed
     * city-to-region table is used, returning {@link #UNKNOWN} for an unknown city.
     */
    public static String locality(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : region(city);
    }

    /**
     * The region whose range contains the given 4-digit postcode, or {@code null} when
     * the postcode is absent, non-numeric, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
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
     * The locality of a stored owner: the {@code REGION} component of its customerCode (see
     * {@link CustomerCode#regionOf(String)}), so the locality follows from the same
     * region-and-hash identity the customerCode carries. Falls back to
     * {@link #locality(String, String)} when the customerCode is absent or carries no region
     * prefix (e.g. legacy records).
     */
    public static String localityOf(String customerCode, String city, String postcode) {
        String region = CustomerCode.regionOf(customerCode);
        return region != null ? region : locality(city, postcode);
    }

    /**
     * The IANA timezone name for an owner's locality, derived from the same customerCode
     * region prefix (falling back to city/postcode) that {@link #localityOf(String, String,
     * String)} yields, via the fixed region-to-timezone table (NSW-&gt;Australia/Sydney,
     * VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane). Returns {@code null} when the
     * locality has no mapped timezone (i.e. {@link #UNKNOWN}).
     */
    public static String timezoneOf(String customerCode, String city, String postcode) {
        return REGION_TIMEZONE.get(localityOf(customerCode, city, postcode));
    }

    /**
     * The inclusive {@code {low, high}} 4-digit postcode range for a region, or {@code null}
     * when the region is not in the fixed table (i.e. {@link #UNKNOWN}), meaning any 4-digit
     * postcode is acceptable.
     */
    public static int[] postcodeRange(String region) {
        return REGION_POSTCODES.get(region);
    }
}
