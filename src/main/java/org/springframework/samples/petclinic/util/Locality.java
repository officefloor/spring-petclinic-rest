package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives the canonical region ('locality') for an owner. The region is resolved by the
 * postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only falls back
 * to the fixed city-to-region table when the postcode is absent or in no known range. This
 * returns the same region for known cities but disambiguates cities that share a name.
 * An owner with neither a known postcode range nor a known city derives 'UNKNOWN'.
 */
public final class Locality {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code UNKNOWN} when not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region, preferring the {@code postcode} range. When the postcode is
     * absent or in no known range, falls back to the {@code city} table (see {@link #of(String)}).
     */
    public static String of(String city, String postcode) {
        String byPostcode = fromPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /** The IANA timezone name for {@code region}, or {@code null} when the region has none. */
    public static String timezone(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /** Region whose inclusive range contains {@code postcode}, or {@code null} if none. */
    private static String fromPostcode(String postcode) {
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
}
