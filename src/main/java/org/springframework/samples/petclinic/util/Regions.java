package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives the canonical region code from an owner's postcode.
 *
 * <p>A region is identified solely by the 4-digit postcode, keyed via a fixed
 * region-to-range table:
 * <ul>
 *     <li>NSW, postcodes 2000-2099;</li>
 *     <li>VIC, postcodes 3000-3099;</li>
 *     <li>QLD, postcodes 4000-4099.</li>
 * </ul>
 *
 * <p>This is the single rule behind the {@code <REGION>} segment of an owner's
 * {@code customerCode} and the owner's derived locality. A {@code null} postcode,
 * a value that is not a 4-digit number, or a 4-digit value outside every known
 * range all derive the region {@code "UNKNOWN"}.
 */
public final class Regions {

    /**
     * Region to its inclusive 4-digit postcode range {@code {low, high}}.
     */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Region to its IANA timezone name, via a fixed region-to-timezone table.
     */
    private static final Map<String, String> REGION_TIMEZONES = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The region code a {@code "UNKNOWN"} postcode (absent, non-4-digit, or in no known range)
     * derives to.
     */
    public static final String UNKNOWN = "UNKNOWN";

    private Regions() {
    }

    /**
     * The canonical region code for the given postcode: the region whose inclusive 4-digit range
     * contains the postcode, or {@link #UNKNOWN} when the postcode is {@code null}, not a 4-digit
     * value, or outside every known range.
     */
    public static String forPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return UNKNOWN;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return UNKNOWN;
    }

    /**
     * The IANA timezone name for the given region code, via a fixed region-to-timezone table
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane), or
     * {@code null} when the region is {@code null} or not in the table (e.g. {@link #UNKNOWN}).
     */
    public static String timezoneForRegion(String region) {
        return region == null ? null : REGION_TIMEZONES.get(region);
    }
}
