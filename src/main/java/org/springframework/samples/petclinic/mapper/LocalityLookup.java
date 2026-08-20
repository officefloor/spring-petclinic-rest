package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (canonical region) by preferring the postcode
 * range, then falling back to a fixed city-to-region table.
 *
 * <p>Deliberately a plain utility class rather than a method on {@link OwnerMapper}:
 * a {@code String -> String} method declared on a MapStruct {@code @Mapper} interface
 * would be picked up as a general property-conversion method and silently applied to
 * every string field. Kept here, it is only ever invoked from an explicit mapping
 * expression, so MapStruct never treats it as a conversion.
 */
public final class LocalityLookup {

    /** City -> canonical region; anything absent derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name; anything absent (e.g. "UNKNOWN") has no timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private LocalityLookup() {
    }

    /**
     * @param region the canonical region (e.g. "NSW")
     * @return the IANA timezone name for {@code region}, or {@code null} when the region
     *         is {@code null} or not in the fixed region-to-timezone table
     */
    public static String timezoneFor(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /**
     * @param city the owner's city (may be {@code null})
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when it is not
     *         in the table
     */
    public static String regionFor(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Derives the canonical region, preferring the postcode: when {@code postcode} is a
     * 4-digit value inside a known region range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) that region wins, disambiguating cities that share a name.
     * Otherwise — postcode absent or in no known range — falls back to the
     * city-to-region table.
     *
     * @param city the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null}/blank)
     * @return the canonical region, or {@code "UNKNOWN"} when neither source resolves one
     */
    public static String regionFor(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : regionFor(city);
    }

    /**
     * Derives the canonical region purely from the postcode, as used by the owner's
     * {@code <REGION>-<HASH8>} customer code. A 4-digit postcode inside a known region range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) yields that region; anything else —
     * postcode absent, not 4 digits, or in no known range — yields {@code "UNKNOWN"}.
     *
     * @param postcode the owner's postcode (may be {@code null}/blank)
     * @return the canonical region for {@code postcode}, or {@code "UNKNOWN"}
     */
    public static String postcodeRegion(String postcode) {
        String region = regionForPostcode(postcode);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * @return the region whose range contains {@code postcode}, or {@code null} when the
     *         postcode is absent, not 4 digits, or in no known range
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
}
