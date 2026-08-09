package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Fixed city-to-region and region-to-postcode-range tables used to validate an owner's
 * postcode against the region derived from the owner's city.
 *
 * <p>Cities: Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD. A city with no known
 * region accepts any 4-digit postcode. Ranges (inclusive): NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099.
 */
public final class PostcodeRegions {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private PostcodeRegions() {
    }

    /** IANA timezone name for the region, or null when the region has no known timezone. */
    public static String timezoneForRegion(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /** Fixed version-2 tag mixed into the region code used inside owner identifiers. */
    private static final String VERSION_TAG = "V2";

    /**
     * The plain region code for a postcode: the canonical region (NSW, VIC, QLD) whose
     * range contains it, or {@code "UNKNOWN"} when there is none. This is the value that
     * surfaces user-facing as {@code locality}/{@code timezone} and in the owner segment,
     * and forms the plain REGION prefix of the memberId — it never carries the version tag.
     */
    public static String regionCode(String postcode) {
        String region = regionForPostcode(postcode);
        return region == null ? "UNKNOWN" : region;
    }

    /**
     * The version-2 region code: the plain {@link #regionCode(String) region code} combined
     * with the fixed {@code 'V2'} version tag. It is mixed <em>inside</em> the owner
     * identifiers (memberId hash, householdId and identityKey) so every version-2 identifier
     * differs from the value version 1 produced, while {@code locality}, {@code timezone} and
     * the owner segment keep the plain region code.
     */
    public static String regionCodeV2(String postcode) {
        return regionCode(postcode) + "-" + VERSION_TAG;
    }

    /** Canonical region for the city, or null when the city has no known region. */
    public static String regionFor(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * Canonical region whose inclusive postcode range contains {@code postcode}, or null
     * when the postcode is blank, not a number, or in no known range.
     */
    public static String regionForPostcode(String postcode) {
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

    /**
     * True when the 4-digit postcode is valid for the city: valid when the city has no
     * known region, otherwise valid only when the postcode falls within that region's
     * inclusive range.
     */
    static boolean isValid(String city, int postcode) {
        String region = regionFor(city);
        if (region == null) {
            return true; // city with no known region accepts any 4-digit postcode
        }
        int[] range = REGION_RANGE.get(region);
        return range == null || (postcode >= range[0] && postcode <= range[1]);
    }
}
