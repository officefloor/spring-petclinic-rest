package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's locality (region) from the postcode and city.
 *
 * <p>The postcode is preferred: a 4-digit postcode falling in a known region range (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099) fixes the region and disambiguates cities that share a name. Only
 * when the postcode is absent or in no known range does derivation fall back to the fixed
 * city-to-region table.
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * {@code String}-to-{@code String} method declared on a MapStruct mapper would be picked up as an
 * implicit conversion and applied to every String property mapping.
 */
public final class OwnerLocality {

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -&gt; IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private OwnerLocality() {
    }

    /**
     * Returns the canonical region, preferring the {@code postcode}: a postcode in a known region
     * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) selects that region. When the postcode is
     * absent, malformed, or in no known range, falls back to the city-to-region table via
     * {@link #derive(String)}.
     */
    public static String derive(String city, String postcode) {
        String fromPostcode = regionForPostcode(postcode);
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return derive(city);
    }

    /**
     * Returns the canonical region for {@code city} (Sydney-&gt;NSW, Melbourne-&gt;VIC,
     * Brisbane-&gt;QLD), or {@code "UNKNOWN"} when the city is not in the table.
     */
    public static String derive(String city) {
        if (city == null) {
            return "UNKNOWN";
        }
        switch (city) {
            case "Sydney":
                return "NSW";
            case "Melbourne":
                return "VIC";
            case "Brisbane":
                return "QLD";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Returns the region an owner's identity resolves to by reading it back off the
     * {@code customerCode}, which is formatted {@code '<REGION>-<HASH8>'}: the portion before the
     * first {@code '-'}. The locality therefore shares the single region-and-hash identity assigned
     * at creation instead of being recomputed from the city and postcode. Returns {@code "UNKNOWN"}
     * when the customer code is absent, blank, or carries no region segment.
     */
    public static String fromCustomerCode(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        String region = dash < 0 ? customerCode : customerCode.substring(0, dash);
        return region.isBlank() ? "UNKNOWN" : region;
    }

    /**
     * Returns the IANA timezone name for {@code region} via the fixed region-to-timezone table
     * (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane), or
     * {@code null} when the region is absent or not in the table.
     */
    public static String timezoneForRegion(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /** Region for a 4-digit {@code postcode} in a known range, or {@code null} otherwise. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
