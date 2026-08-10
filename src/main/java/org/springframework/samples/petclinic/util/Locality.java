package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives the canonical region ('locality') for an owner. The region is resolved by the
 * postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only falls back
 * to the fixed city-to-region table when the postcode is absent or in no known range. This
 * returns the same region for known cities but disambiguates cities that share a name.
 * An owner with neither a known postcode range nor a known city derives 'UNKNOWN'.
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

    public static final String UNKNOWN = "UNKNOWN";

    /**
     * The fixed version-2 tag mixed into the identifiers. It is embedded only inside the derived
     * identifiers (memberId, householdId, identityKey via {@link #identityRegion}); it never appears
     * in the user-facing 'locality' (which stays the plain region such as 'NSW'), the timezone, or the
     * owner segment's derived region.
     */
    public static final String IDENTITY_VERSION_TAG = "V2";

    private Locality() {
    }

    /** The canonical region for {@code city}, or {@code UNKNOWN} when not in the table. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region, preferring the {@code postcode} range. When the postcode is
     * absent or in no known range, falls back to the {@code city} table (see {@link #of(String)}).
     */
    public static String of(String city, String postcode) {
        String byPostcode = fromPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /**
     * The version-2 region code embedded <em>inside</em> the identifiers: the plain region (see
     * {@link #of(String, String)}) with the fixed {@link #IDENTITY_VERSION_TAG 'V2'} tag mixed in as a
     * prefix (e.g. 'NSW' becomes 'V2NSW'). Because every version-1 region is a plain letter code, no
     * version-2 identifier can reproduce a value produced under version 1. The user-facing 'locality'
     * keeps using {@link #of(String, String)} and stays the plain region.
     */
    public static String identityRegion(String city, String postcode) {
        return IDENTITY_VERSION_TAG + of(city, postcode);
    }

    /** The IANA timezone name for {@code region}, or {@code null} when the region has none. */
    public static String timezone(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /** Region whose inclusive range contains {@code postcode}, or {@code null} if none. */
    private static String fromPostcode(String postcode) {
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
