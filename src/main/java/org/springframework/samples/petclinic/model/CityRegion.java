package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * The pinned region reference data used to derive an owner's locality. Knows both the
 * city-to-region table and each region's inclusive four-digit postcode range (NSW
 * 2000-2099, VIC 3000-3099, QLD 4000-4099).
 *
 * <p>Locality is derived by postcode first — {@link #localityOf(String, String)} looks up
 * the region whose range contains the postcode and only falls back to the city-to-region
 * table when the postcode is absent or in no known range. A city or postcode with no known
 * region resolves to {@link #UNKNOWN}.
 */
public final class CityRegion {

    /** Locality returned when neither postcode nor city resolves to a known region. */
    public static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_TO_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive {low, high} four-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private CityRegion() {
    }

    /**
     * The canonical region for the given city, or {@link #UNKNOWN} when the city is not
     * in the table (including a {@code null} city).
     */
    public static String localityOf(String city) {
        return CITY_TO_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region, preferring the postcode: the region whose pinned range
     * contains {@code postcode}, falling back to the {@link #localityOf(String) city
     * table} when the postcode is absent or in no known range. This disambiguates cities
     * that share a name while still resolving known cities to the same region.
     */
    public static String localityOf(String postcode, String city) {
        String byPostcode = regionOfPostcode(postcode);
        return byPostcode != null ? byPostcode : localityOf(city);
    }

    /**
     * The region whose pinned range contains {@code postcode}, or {@code null} when the
     * postcode is {@code null}, not four digits, or in no known range.
     */
    public static String regionOfPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The inclusive {low, high} four-digit postcode range pinned to {@code region}, or
     * {@code null} when the region has no pinned range.
     */
    public static int[] rangeOf(String region) {
        int[] range = REGION_RANGES.get(region);
        return range == null ? null : range.clone();
    }
}
