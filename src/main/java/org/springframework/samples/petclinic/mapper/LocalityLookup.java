package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality'). The postcode is preferred: the region is
 * looked up by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), falling back
 * to a fixed city-to-region table when the postcode is absent or in no known range. Cities absent
 * from the table (and postcodes in no range) resolve to 'UNKNOWN'.
 */
public final class LocalityLookup {

    private static final String UNKNOWN = "UNKNOWN";

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private LocalityLookup() {
    }

    /** The canonical region for {@code city}, or 'UNKNOWN' when it is not in the table. */
    public static String regionOf(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region, preferring the postcode: match {@code postcode} against the known
     * region ranges first, and only fall back to {@link #regionOf(String)} when the postcode is
     * absent or in no known range.
     */
    public static String regionOf(String city, String postcode) {
        String byPostcode = regionOfPostcode(postcode);
        return byPostcode != null ? byPostcode : regionOf(city);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
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
