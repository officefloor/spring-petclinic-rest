package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives an owner's 'locality' (canonical region). The postcode is preferred: a
 * 4-digit postcode falling in a known region range (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099) yields that region. Only when the postcode is absent or in no known
 * range does it fall back to the fixed city-to-region table. Any city not in the table
 * — including a null city — yields "UNKNOWN".
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

    /** Sentinel returned by {@link #of(String)} for a city with no known region. */
    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /** A syntactically valid postcode is exactly 4 digits. */
    private static final java.util.regex.Pattern FOUR_DIGITS = java.util.regex.Pattern.compile("\\d{4}");

    /** Canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Canonical region for an owner, preferring the {@code postcode}: when it falls in a known
     * region range this returns that region, disambiguating cities that share a name. Otherwise
     * (postcode absent or in no known range) it falls back to the city-to-region table via
     * {@link #of(String)}.
     */
    public static String of(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /**
     * The region whose inclusive postcode range contains {@code postcode}, or {@code null} when
     * the postcode is absent, not 4 digits, or in no known range.
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
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
     * The inclusive 4-digit postcode range {low, high} for {@code region}, or {@code null}
     * when the region has no known range (so any 4-digit postcode is acceptable).
     */
    public static int[] postcodeRange(String region) {
        return REGION_POSTCODES.get(region);
    }

    /**
     * The IANA timezone name for {@code region} via the fixed region-to-timezone table
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane), or
     * {@code null} when the region is absent or not in the table.
     */
    public static String timezone(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }
}
