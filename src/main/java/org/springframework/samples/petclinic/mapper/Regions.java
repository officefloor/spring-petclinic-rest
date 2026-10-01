package org.springframework.samples.petclinic.mapper;

import java.util.Map;

import org.springframework.samples.petclinic.model.MemberId;

/**
 * City-to-region lookup backing the {@code locality} field produced by
 * {@link OwnerMapper#toOwnerDto}. The table pins each known city to its canonical region string; any
 * city not listed derives the locality {@code "UNKNOWN"}.
 */
public final class Regions {

    /** City -> canonical region. Anything not listed derives locality {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. A region absent from this table (and
     *  the {@link #UNKNOWN} locality) imposes no postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. A region absent from this table (including the {@link #UNKNOWN}
     *  locality) has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /** The locality returned for a city absent from {@link #CITY_REGION}. Points at the single
     *  {@link MemberId#UNKNOWN_REGION} definition so the region baked into a member id and the
     *  region/locality derived here share one sentinel. */
    public static final String UNKNOWN = MemberId.UNKNOWN_REGION;

    private Regions() {
    }

    /**
     * Whether a 4-digit postcode is valid for the given city. A city whose region has no pinned
     * postcode range (including an unknown city) accepts any value.
     *
     * @param city     the owner's city; may be {@code null}
     * @param postcode the already-4-digit postcode to check
     * @return {@code true} when the city's region imposes no range or the postcode falls within it
     */
    public static boolean postcodeMatchesCity(String city, String postcode) {
        int[] range = REGION_POSTCODES.get(localityOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Derive the locality (canonical region) preferring the postcode. The postcode's region range is
     * consulted first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); only when the postcode is absent
     * or falls in no known range does the city-to-region table decide. This returns the same region
     * for known cities but disambiguates cities that share a name.
     *
     * @param city     the owner's city; may be {@code null}
     * @param postcode the owner's postcode; may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when neither postcode nor city resolves
     */
    public static String localityOf(String city, String postcode) {
        String byPostcode = regionOfPostcode(postcode);
        return byPostcode != null ? byPostcode : localityOf(city);
    }

    /**
     * Derive the region code purely from the postcode. This is the {@code REGION} component of an
     * owner's {@code <REGION><FY><HASH8><CHK>} member id: the region whose range contains the postcode, or
     * {@link #UNKNOWN} when the postcode is absent, non-numeric, or falls in no known range. Unlike
     * {@link #localityOf(String, String)} it never falls back to the city.
     *
     * @param postcode the owner's postcode; may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the postcode resolves to no region
     */
    public static String regionForPostcode(String postcode) {
        String region = regionOfPostcode(postcode);
        return region != null ? region : UNKNOWN;
    }

    /**
     * The region code baked <em>inside</em> an owner's version-2 {@code memberId}: the plain
     * {@link #regionForPostcode(String) region} with the fixed
     * {@link org.springframework.samples.petclinic.model.IdentityVersion#TAG identity-version tag} mixed
     * in, e.g. {@code NSWV2} (or {@code UNKNOWNV2}). The tag lives only here, inside the identifier, so
     * the member id differs from its version-1 form; it never reaches the user-facing {@code locality},
     * {@code timezone} or owner segment, which keep the plain region from {@link #regionForPostcode}.
     *
     * @param postcode the owner's postcode; may be {@code null}
     * @return the plain region followed by the version tag
     */
    public static String regionForPostcodeV2(String postcode) {
        return regionForPostcode(postcode) + org.springframework.samples.petclinic.model.IdentityVersion.TAG;
    }

    /**
     * Derive the locality (canonical region) for a city.
     *
     * @param city the owner's city; may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String localityOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Whether {@code region} is a known, pinned region (NSW, VIC or QLD) rather than the
     * {@link #UNKNOWN} sentinel or some other value. Backed by the same {@link #REGION_TIMEZONE}
     * table the other region facts are derived from, so the set of known regions stays single-sourced
     * here and cannot drift from a copy held elsewhere.
     *
     * @param region the canonical region code (e.g. an owner's {@code locality}); may be {@code null}
     * @return {@code true} when {@code region} is one of the pinned regions
     */
    public static boolean isKnownRegion(String region) {
        return REGION_TIMEZONE.containsKey(region);
    }

    /**
     * The IANA timezone name for a region, taken from the pinned region-to-timezone table
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane).
     *
     * @param region the canonical region code (e.g. the owner's {@code locality}); may be {@code null}
     * @return the IANA timezone name, or {@code null} when the region has no pinned timezone
     */
    public static String timezoneOf(String region) {
        return REGION_TIMEZONE.get(region);
    }

    /**
     * The region whose postcode range contains the given postcode, or {@code null} when the postcode
     * is absent, non-numeric, or falls in no known range.
     */
    private static String regionOfPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
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
