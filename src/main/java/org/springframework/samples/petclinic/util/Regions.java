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
 * {@code memberId} and the owner's derived locality. A {@code null} postcode,
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

    /**
     * The fixed version tag mixed into every version-2 owner identifier, and the single source of
     * truth for that tag (referenced by
     * {@code org.springframework.samples.petclinic.model.Owner#IDENTITY_VERSION_TAG}). It is
     * appended to the plain region code (see {@link #forPostcodeV2(String)}), so the
     * {@code memberId}'s region segment carries it, and is also mixed into the {@code identityKey}
     * and {@code householdId} derivations; the user-facing locality, timezone and owner-segment
     * region keep the plain region code.
     */
    public static final String VERSION_TAG = "V2";

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
     * The version-2 region code used inside the identifiers for the given postcode: the plain
     * region code (see {@link #forPostcode(String)}) with the fixed {@link #VERSION_TAG} appended,
     * e.g. {@code "NSWV2"} or {@code "UNKNOWNV2"}. Mixing in the version tag means the
     * {@code memberId} it forms the region segment of differs from the value the version-1
     * algorithm produced. This is used only inside the identifiers; the user-facing locality,
     * timezone and owner-segment region use the plain {@link #forPostcode(String)} region code.
     */
    public static String forPostcodeV2(String postcode) {
        return forPostcode(postcode) + VERSION_TAG;
    }

    /**
     * Whether the given region code is a known region, i.e. one of the regions in the
     * region-to-range table (NSW, VIC or QLD). {@code null} and {@link #UNKNOWN}, along with any
     * other value, are not known regions.
     */
    public static boolean isKnownRegion(String region) {
        return region != null && REGION_RANGES.containsKey(region);
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
