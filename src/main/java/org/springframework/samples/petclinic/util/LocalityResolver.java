package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (region). The postcode is preferred: a present,
 * well-formed postcode is matched against each region's fixed range (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). Only when the postcode is absent or falls in no known
 * range does derivation fall back to a fixed city-to-region table. Anything the postcode
 * and city both fail to place derives the locality {@code "UNKNOWN"}.
 */
public final class LocalityResolver {

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    /** The locality returned when neither postcode nor city places the owner. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     * city (including {@code null}) is not in the table.
     */
    public static String localityOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Returns the canonical region, preferring {@code postcode}: when it is present and
     * falls in a known region's range that region wins. Otherwise the {@code city} table
     * decides, and failing that the locality is {@code "UNKNOWN"}. This returns the same
     * region for known cities but disambiguates cities that share a name.
     */
    public static String localityOf(String postcode, String city) {
        String fromPostcode = regionForPostcode(postcode);
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return localityOf(city);
    }

    /**
     * The {@code REGION} component carried by a {@code customerCode} of the form
     * {@code <REGION>-<HASH8>}. Now that the customer code embeds the region, the owner's locality
     * is read straight back off the identity rather than recomputed from postcode and city.
     * Returns {@code "UNKNOWN"} when the code is absent or carries no region component.
     */
    public static String regionOfCustomerCode(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            return UNKNOWN;
        }
        int dash = customerCode.indexOf('-');
        if (dash <= 0) {
            return UNKNOWN;
        }
        return customerCode.substring(0, dash);
    }

    /**
     * Whether {@code region} is one of the known regions (NSW, VIC or QLD) rather than
     * {@code "UNKNOWN"} or any unrecognised value (including {@code null}).
     */
    public static boolean isKnownRegion(String region) {
        return REGION_TIMEZONE.containsKey(region);
    }

    /**
     * The IANA timezone name for {@code region} via the fixed region-to-timezone table
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane).
     * Returns {@code null} when the region (including {@code null} or {@code "UNKNOWN"}) is not
     * in the table.
     */
    public static String timezoneOfRegion(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when absent/unplaced. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
