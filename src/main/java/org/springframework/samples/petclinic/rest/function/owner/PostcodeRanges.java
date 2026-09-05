package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Fixed region-to-postcode-range table used to validate an owner's postcode against the region
 * derived from its city (see {@link Locality}): NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A region
 * with no entry (including {@code "UNKNOWN"}) has no range, so any 4-digit postcode is accepted.
 */
public final class PostcodeRanges {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private PostcodeRanges() {
    }

    /** The inclusive {low, high} range for {@code region}, or {@code null} when the region is unknown. */
    public static int[] forRegion(String region) {
        return REGION_POSTCODES.get(region);
    }

    /**
     * The region whose range contains {@code postcode}, or {@code null} when the postcode is absent,
     * not four digits, or in no known range.
     */
    public static String regionFor(String postcode) {
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
