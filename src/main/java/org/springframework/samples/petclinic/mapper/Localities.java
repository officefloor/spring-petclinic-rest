package org.springframework.samples.petclinic.mapper;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's canonical region ("locality"). The postcode is preferred:
 * a 4-digit postcode is matched against each region's inclusive range first, and
 * only when the postcode is absent or in no known range does derivation fall back
 * to a fixed city-to-region table. Kept as a plain utility (rather than a mapper
 * method) so MapStruct does not treat it as an implicit {@code String -> String}
 * mapping.
 */
public final class Localities {

    /** City -> canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. Anything not listed derives a {@code null} timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /** Region returned for a city that is absent from {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * Returns the canonical region for {@code city} from the fixed table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}),
     * or {@code "UNKNOWN"} when the city is {@code null} or not in the table.
     *
     * @param city the owner's city
     * @return the canonical region string, or {@code "UNKNOWN"} when unknown
     */
    public static String forCity(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Returns the canonical region, preferring the {@code postcode}: it is matched
     * against each region's inclusive postcode range first (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099). Only when the postcode is {@code null},
     * non-numeric, or in no known range does derivation fall back to the
     * {@link #forCity(String) city-to-region table}. This returns the same region
     * for known cities but disambiguates cities that share a name.
     *
     * @param city the owner's city
     * @param postcode the owner's postcode, may be {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when unknown
     */
    public static String forCityAndPostcode(String city, String postcode) {
        String region = forPostcode(postcode);
        if (region != null) {
            return region;
        }
        return forCity(city);
    }

    /**
     * Returns the owner's canonical region ("locality"), the plain region derived from the owner's
     * city and postcode via {@link #forCityAndPostcode(String, String)}. This is the same base
     * region the version-2 identity is built on, but without the {@code 'V2'} version tag: the tag
     * appears only inside the identifiers (see {@link #regionCodeV2(String, String)}), never in the
     * user-facing locality, timezone or owner segment.
     *
     * @param owner the owner whose locality is required
     * @return the canonical region string, or {@code "UNKNOWN"} when unknown
     */
    public static String forOwner(Owner owner) {
        return forCityAndPostcode(owner.getCity(), owner.getPostcode());
    }

    /**
     * Returns the version-2 region code used <em>inside</em> the owner's identifiers, formed by
     * mixing the fixed {@code 'V2'} version tag ({@link Owner#IDENTITY_VERSION_TAG}) into the plain
     * canonical region ({@link #forCityAndPostcode(String, String)}). For example a Sydney owner
     * (region {@code NSW}) yields {@code 'V2NSW'}. This appears only in identifiers (the memberId's
     * region segment); the user-facing {@link #forOwner(Owner) locality} stays the plain region.
     *
     * @param city     the owner's city
     * @param postcode the owner's postcode, may be {@code null}
     * @return the version-2 region code
     */
    public static String regionCodeV2(String city, String postcode) {
        return Owner.IDENTITY_VERSION_TAG + forCityAndPostcode(city, postcode);
    }

    /**
     * Returns the owner's segment, formatted {@code "<TIER>_<AREA>"}. TIER is {@code "PREMIUM"}
     * when the owner's {@link Owner#effectiveMembershipLevel(Owner) effective membership level} is
     * 3 or more, otherwise {@code "STANDARD"}. AREA is {@code "METRO"} when the owner's
     * {@link #forOwner(Owner) locality} is a known region (NSW, VIC or QLD), otherwise
     * {@code "REGIONAL"}.
     *
     * @param owner the owner whose segment is required
     * @return the segment string, one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
     * {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}
     */
    public static String segmentForOwner(Owner owner) {
        String tier = Owner.effectiveMembershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_POSTCODES.containsKey(forOwner(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Returns the IANA timezone name for {@code region} from the fixed region-to-timezone
     * table ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}), or {@code null} when the region is {@code null}
     * or not in the table (for example {@code "UNKNOWN"}).
     *
     * @param region the canonical region string
     * @return the IANA timezone name, or {@code null} when unknown
     */
    public static String timezoneForRegion(String region) {
        if (region == null) {
            return null;
        }
        return REGION_TIMEZONE.get(region);
    }

    /**
     * Returns the owner's IANA timezone name, derived from the owner's locality/region
     * ({@link #forOwner(Owner)}) via the fixed region-to-timezone table, or {@code null}
     * when the region is not in the table.
     *
     * @param owner the owner whose timezone is required
     * @return the IANA timezone name, or {@code null} when unknown
     */
    public static String timezoneForOwner(Owner owner) {
        return timezoneForRegion(forOwner(owner));
    }

    /**
     * Returns the region whose inclusive postcode range contains {@code postcode},
     * or {@code null} when the postcode is {@code null}, non-numeric, or in no
     * known range.
     */
    private static String forPostcode(String postcode) {
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
