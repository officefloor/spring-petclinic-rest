package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single source of truth for an owner's canonical region, derived from the postcode and city. A
 * region is a short code (NSW, VIC, QLD) fixed by two lookup tables: the inclusive 4-digit postcode
 * range per region, and the city-to-region fallback used when the postcode yields nothing.
 *
 * <p>{@link #fromPostcode} answers the region for a postcode alone (or {@code null});
 * {@link #identifierRegionCode} is the one derivation the identifiers embed — the version-tagged REGION
 * segment of an owner's {@code memberId} (e.g. {@code NSWV2}) — folding an absent region to
 * {@code UNKNOWN}; {@link #regionOf} reads the <em>plain</em> region back out of a {@code memberId}
 * (stripping the version tag), which is how a whole owner resolves to a locality.
 */
public final class OwnerRegion {

    /** City -> canonical region, from the fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name, from the fixed region-to-timezone table. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private OwnerRegion() {
    }

    /** The canonical region for {@code city} from the fixed city-to-region table, or {@code null}. */
    static String regionForCity(String city) {
        return city == null ? null : CITY_REGION.get(city);
    }

    /** The known region codes (NSW, VIC, QLD) — the prefixes a memberId's REGION segment can take. */
    static Iterable<String> knownRegions() {
        return REGION_TIMEZONE.keySet();
    }

    /** The inclusive 4-digit postcode range {low, high} for {@code region}, or {@code null}. */
    static int[] rangeForRegion(String region) {
        return REGION_RANGE.get(region);
    }

    /**
     * The region whose fixed range contains {@code postcode} (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), or {@code null} when the postcode is absent, non-numeric, or in no known range.
     */
    public static String fromPostcode(String postcode) {
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
     * The version-2 region code the identifiers embed — the REGION segment of an owner's
     * {@code memberId}, derived from the postcode: the plain region ({@link #fromPostcode}) when the
     * postcode maps to a known region, else {@code UNKNOWN}, with the fixed {@link OwnerIdentity#VERSION_TAG}
     * appended (e.g. {@code NSWV2}, {@code UNKNOWNV2}). This is the single source for the region that goes
     * <em>into</em> an identifier; mixing in the version tag re-derives the memberId under version 2 so
     * it differs from its version-1 form. It is kept distinct from the user-facing locality that
     * {@link #regionOf} reads back <em>out</em> of an assigned id as the plain region (no version tag).
     */
    public static String identifierRegionCode(String postcode) {
        String region = fromPostcode(postcode);
        return (region == null ? "UNKNOWN" : region) + OwnerIdentity.VERSION_TAG;
    }

    /**
     * The <em>plain</em> region of a {@code memberId} ({@code <REGIONV2><FY><HASH8><CHK>}) — the owner's
     * locality. The version tag carried by the memberId's region segment (e.g. {@code NSWV2}) is
     * stripped, so the locality is the plain region (e.g. {@code NSW}) read straight off the assigned
     * memberId and always matches the id. {@code UNKNOWN} when the id is absent or has no known region
     * segment.
     */
    public static String regionOf(String memberId) {
        return MemberId.regionOf(memberId);
    }

    /**
     * The IANA timezone name for the owner's plain region, resolved from the (version-tagged) region
     * segment of the {@code memberId} through the fixed region-to-timezone table (NSW -> Australia/Sydney,
     * VIC -> Australia/Melbourne, QLD -> Australia/Brisbane). {@code null} when the region is
     * absent or unknown.
     */
    public static String timezoneOf(String memberId) {
        return REGION_TIMEZONE.get(regionOf(memberId));
    }
}
