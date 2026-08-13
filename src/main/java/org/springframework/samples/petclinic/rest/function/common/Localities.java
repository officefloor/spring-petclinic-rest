package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region). The postcode is preferred: the region is looked
 * up by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only when the
 * postcode is absent or falls in no known range does it fall back to the fixed city-to-region
 * table. Anything unresolved maps to {@link #UNKNOWN}.
 *
 * <p>{@link #ofPostcode(String)} resolves the region from the postcode alone — the {@code REGION}
 * segment of an owner's {@code memberId} — and {@link #regionOf(String)} reads that segment
 * back out of a stored {@code memberId}.
 */
public final class Localities {

    /** Value returned when neither the postcode nor the city resolves a region. */
    public static final String UNKNOWN = "UNKNOWN";

    /**
     * The fixed version-2 tag mixed into every owner identifier (memberId, householdId and
     * identityKey) so each is rederived and no value produced under version 1 is produced again.
     * The tag lives only inside the identifiers — never in the user-facing {@code locality},
     * {@code timezone} or the owner segment's derived region, which stay the plain region code.
     */
    public static final String IDENTITY_VERSION = "V2";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

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

    private Localities() {
    }

    /**
     * The canonical region for {@code city} using only the city-to-region table, or
     * {@code UNKNOWN} when not in the table.
     */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region, preferring the postcode: resolves by postcode range first and only
     * falls back to the city-to-region table when the postcode is absent or in no known range.
     * Returns {@code UNKNOWN} when neither resolves.
     */
    public static String of(String city, String postcode) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /**
     * The canonical region derived from the postcode ALONE (no city fallback): the region whose
     * range contains {@code postcode}, or {@code UNKNOWN} when the postcode is absent or in no known
     * range. This is the {@code REGION} segment of an owner's {@code memberId}.
     */
    public static String ofPostcode(String postcode) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : UNKNOWN;
    }

    /**
     * The version-2 region code embedded INSIDE an owner's identifiers: the {@link #IDENTITY_VERSION}
     * tag prefixed to the plain {@link #ofPostcode(String) postcode region} (e.g. {@code 2000 ->
     * V2NSW}, absent/unknown postcode {@code -> V2UNKNOWN}). Because every version-1 region is a bare
     * letters run (NSW, VIC, QLD, UNKNOWN) and this one begins {@code V2}, no version-1 identifier is
     * reproduced. This is the {@code REGION} segment of the version-2 {@code memberId}; the plain
     * region ({@link #ofPostcode}) is what the user-facing {@code locality}, {@code timezone} and the
     * owner segment continue to use.
     */
    public static String identityRegion(String postcode) {
        return IDENTITY_VERSION + ofPostcode(postcode);
    }

    /**
     * The IANA timezone name for the plain region a {@code postcode} resolves to via the fixed
     * region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
     * QLD -> Australia/Brisbane). Returns {@code UNKNOWN} when the region is absent or not in the
     * table. The version tag never enters here — the timezone follows the plain region.
     */
    public static String timezoneOfPostcode(String postcode) {
        return REGION_TIMEZONE.getOrDefault(ofPostcode(postcode), UNKNOWN);
    }

    /**
     * The region an owner belongs to, read back from the leading {@code REGION} segment of its
     * {@code memberId} ({@code <REGION><FY><HASH8><CHK>}) — the single region-and-hash identity all
     * region-derived values now flow from. The region is the leading run of letters (the FY digits
     * that follow it terminate the segment). Returns {@code UNKNOWN} when the id is absent or has no
     * leading letters.
     */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return UNKNOWN;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? UNKNOWN : memberId.substring(0, i);
    }

    /**
     * The IANA timezone name for an owner, derived from the {@code REGION} segment of its
     * {@code memberId} via the fixed region-to-timezone table (NSW -> Australia/Sydney,
     * VIC -> Australia/Melbourne, QLD -> Australia/Brisbane). Returns {@code UNKNOWN} when the
     * region is absent or not in the table.
     */
    public static String timezoneOf(String memberId) {
        return REGION_TIMEZONE.getOrDefault(regionOf(memberId), UNKNOWN);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
    private static String byPostcode(String postcode) {
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
