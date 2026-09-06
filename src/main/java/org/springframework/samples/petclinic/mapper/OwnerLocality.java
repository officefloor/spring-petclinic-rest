package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's locality (region), preferring the postcode: the region is
 * looked up by postcode range first, and only when the postcode is absent or in
 * no known range does it fall back to a fixed city-to-region table. This returns
 * the same region for the known cities but disambiguates cities that share a name.
 *
 * <p>Kept as a plain static helper - rather than a method on {@link OwnerMapper} -
 * so MapStruct does not mistake it for an implicit String-to-String property
 * mapping and apply it to unrelated fields.
 *
 * <p>Public, like its sibling {@link MembershipLevel}, so the single region table
 * can be reused wherever an owner's region is needed rather than being
 * duplicated - callers outside this package derive the region through
 * {@link #forPostcodeAndCity(String, String)} (or {@link #forCity(String)} when
 * only the city is available) instead of holding their own copy of the tables.
 */
public final class OwnerLocality {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone; anything not listed derives no timezone (null). */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private OwnerLocality() {
    }

    /**
     * Returns the canonical region for the given owner, preferring the postcode:
     * the region is derived from the postcode range first, falling back to the
     * city-to-region table only when the postcode is absent or in no known range.
     *
     * @param postcode the owner's postcode, or {@code null} when none was supplied
     * @param city the owner's city
     * @return the derived locality (region)
     */
    public static String forPostcodeAndCity(String postcode, String city) {
        String region = forPostcode(postcode);
        return region != null ? region : forCity(city);
    }

    /**
     * Returns the canonical region for the given city, or "UNKNOWN" when the city
     * is not in the fixed table (or is {@code null}).
     *
     * @param city the owner's city
     * @return the derived locality (region)
     */
    public static String forCity(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the IANA timezone for the given region via the fixed region-to-timezone
     * table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD ->
     * Australia/Brisbane), or {@code null} when the region has no known timezone.
     *
     * @param region the owner's locality (region)
     * @return the IANA timezone name, or {@code null} when none is known
     */
    public static String timezoneForRegion(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /**
     * Returns whether the given postcode falls within the postcode range of the
     * region derived from the city. A city whose region has no fixed range (locality
     * "UNKNOWN") accepts any postcode. A postcode that is absent or non-numeric is
     * treated as not matching a ranged region.
     *
     * <p>Callers validate a supplied postcode's four-digit shape separately; this
     * method only answers whether the value sits in the city's region range, so the
     * single region table is not duplicated at the call site.
     *
     * @param postcode the owner's postcode, or {@code null} when none was supplied
     * @param city the owner's city, whose region determines the acceptable range
     * @return {@code true} when the city's region has no fixed range, or the postcode
     *         lies within it
     */
    public static boolean postcodeMatchesCity(String postcode, String city) {
        int[] range = REGION_POSTCODES.get(forCity(city));
        if (range == null) {
            return true;
        }
        if (postcode == null) {
            return false;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException e) {
            return false;
        }
        return value >= range[0] && value <= range[1];
    }

    /**
     * Returns the region whose postcode range contains the given postcode, or
     * {@code null} when the postcode is missing, non-numeric or in no known range.
     */
    private static String forPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException e) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

}
