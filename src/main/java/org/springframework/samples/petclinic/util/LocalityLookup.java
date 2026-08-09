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

    /** Region -&gt; IANA timezone; anything not listed has no timezone. */
    private static final Map<String, String> REGION_TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private LocalityLookup() {
    }

    /**
     * Returns an owner's locality from their region-and-hash customer code, falling back to the
     * postcode-and-city derivation when no customer code is present.
     *
     * <p>Since an owner's {@code customerCode} is formatted {@code <REGION>-<HASH8>}, the locality is
     * the region component that precedes the first {@code '-'}. Owners without a customer code (for
     * example seed data that predates identity assignment) fall back to
     * {@link #forPostcodeAndCity(String, String)}.
     *
     * @param customerCode the owner's region-and-hash customer code, or {@code null}
     * @param postcode the owner's stored postcode, or {@code null} (used only for the fallback)
     * @param city the owner's stored city, or {@code null} (used only for the fallback)
     * @return the region component of the customer code, or the postcode-and-city fallback
     */
    public static String forCustomerCode(String customerCode, String postcode, String city) {
        if (customerCode != null) {
            int separator = customerCode.indexOf('-');
            if (separator > 0) {
                return customerCode.substring(0, separator);
            }
        }
        return forPostcodeAndCity(postcode, city);
    }

    /**
     * Returns the IANA timezone name for the locality derived from the owner's customer code,
     * postcode and city, using the same derivation as {@link #forCustomerCode(String, String, String)}.
     *
     * <p>The derived region is mapped through the fixed region-to-timezone table
     * (NSW -&gt; {@code Australia/Sydney}, VIC -&gt; {@code Australia/Melbourne},
     * QLD -&gt; {@code Australia/Brisbane}). Regions not in the table (including {@code "UNKNOWN"})
     * have no timezone and yield {@code null}.
     *
     * @param customerCode the owner's region-and-hash customer code, or {@code null}
     * @param postcode the owner's stored postcode, or {@code null}
     * @param city the owner's stored city, or {@code null}
     * @return the IANA timezone name for the derived region, or {@code null} when it has none
     */
    public static String timezoneForCustomerCode(String customerCode, String postcode, String city) {
        return REGION_TIMEZONE.get(forCustomerCode(customerCode, postcode, city));
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
