package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Derives an owner's locality (region), preferring the postcode: the postcode range is
 * looked up first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only when the
 * postcode is absent or in no known range does it fall back to the fixed city-to-region
 * table. This returns the same region for known cities but disambiguates cities that
 * share a name.
 */
public final class Locality {

    /** A postcode must be exactly four digits to derive a region. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** City -> canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Returned when the region can be derived from neither postcode nor city. */
    public static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /**
     * The canonical region string for the given city, or {@link #UNKNOWN} when the
     * city is null or not in the fixed city-to-region table.
     */
    public static String of(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region string derived from the postcode range first (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099), falling back to the city-to-region table when the
     * postcode is null, malformed, or in no known range. {@link #UNKNOWN} when neither
     * yields a region.
     */
    public static String of(String city, String postcode) {
        String byPostcode = fromPostcode(postcode);
        if (!UNKNOWN.equals(byPostcode)) {
            return byPostcode;
        }
        return of(city);
    }

    /**
     * The canonical region string derived from the postcode alone (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099), with no city fallback. This is the plain region and
     * never carries a version tag. {@link #UNKNOWN} when the postcode is null, malformed,
     * or in no known range.
     */
    public static String regionOf(String postcode) {
        return fromPostcode(postcode);
    }

    /**
     * The version-2 region code used <em>inside</em> an owner's {@code memberId}: the plain
     * {@link #regionOf(String)} region with the fixed {@link IdentityVersion#TAG} version tag
     * mixed in (e.g. postcode 2000 yields {@code 'NSWV2'}). Because the tag is appended, this
     * can never equal a version-1 region, so no version-1 identifier is produced again. The
     * tag lives only here, inside the identifier — the user-facing locality stays plain.
     */
    public static String identityRegionOf(String postcode) {
        return regionOf(postcode) + IdentityVersion.TAG;
    }

    /**
     * The owner's user-facing locality: the plain region code (e.g. {@code 'NSW'}), derived
     * from the owner's postcode first and city second. This is <em>not</em> an identifier, so
     * it never carries the {@link IdentityVersion#TAG} version tag that appears inside the
     * {@code memberId}. {@link #UNKNOWN} when neither postcode nor city yields a region.
     */
    public static String of(Owner owner) {
        return of(owner.getCity(), owner.getPostcode());
    }

    /** Region whose range contains the 4-digit postcode, or {@link #UNKNOWN}. */
    private static String fromPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return UNKNOWN;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return UNKNOWN;
    }
}
