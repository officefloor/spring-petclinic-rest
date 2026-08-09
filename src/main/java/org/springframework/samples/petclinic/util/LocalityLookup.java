package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's locality (region) from their postcode, falling back to their city.
 *
 * <p>The postcode is matched against fixed 4-digit ranges (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). When the postcode is absent or falls in no known range, the region is looked up
 * in a fixed city-to-region table that pins Sydney -&gt; NSW, Melbourne -&gt; VIC and
 * Brisbane -&gt; QLD. Anything that matches neither derives the locality {@code "UNKNOWN"}.
 */
public final class LocalityLookup {

    /** City -&gt; canonical region; anything not listed derives locality {@code "UNKNOWN"}. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private LocalityLookup() {
    }

    /**
     * Returns the canonical region for the given postcode and city, preferring the postcode.
     *
     * <p>The postcode is looked up by range first; only when it is absent or in no known range does
     * the city-to-region table decide. This returns the same region for known cities but
     * disambiguates cities that share a name.
     *
     * @param postcode the owner's stored postcode, or {@code null}
     * @param city the owner's stored city, or {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when neither postcode nor city maps
     */
    public static String forPostcodeAndCity(String postcode, String city) {
        String region = forPostcode(postcode);
        return region != null ? region : forCity(city);
    }

    /**
     * Returns the canonical region for the given city, or {@code "UNKNOWN"} when the city is not in
     * the fixed city-to-region table (including a {@code null} city).
     *
     * @param city the owner's stored city, or {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the canonical region whose postcode range contains the given postcode, or
     * {@code null} when the postcode is absent, non-numeric, or in no known range.
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
