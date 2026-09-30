package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * The single definition of the region facts: the city-to-region table (Sydney-&gt;NSW,
 * Melbourne-&gt;VIC, Brisbane-&gt;QLD) used to validate a postcode against a city's region, and the
 * postcode-to-region ranges used to derive an owner's identity region. The request-handling layer
 * (which validates a postcode against the city's region and stamps the {@code memberId}'s
 * {@code <REGION>} prefix) and the mapping layer (which derives an owner's {@code locality}) both
 * depend on this one class, so the memberId region and the locality are always the same value.
 *
 * <p>Lives beside {@link Owner} — the same home as {@link OwnerIdentity} — so both layers can
 * depend on it without a package cycle.
 */
public final class CityRegions {

    /** Region reported for a city that is not in the table. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -&gt; canonical region; anything not listed has no known region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -&gt; IANA timezone; anything not listed has no known timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    private CityRegions() {
    }

    /** The city's canonical region, or {@code null} when the city has no known region. */
    public static String regionOf(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The region's inclusive 4-digit postcode range {@code {low, high}}, or {@code null} when the
     * region has no known range. The returned array is a fresh copy, so callers cannot mutate the
     * shared table.
     */
    public static int[] postcodeRange(String region) {
        int[] range = REGION_POSTCODES.get(region);
        return range == null ? null : range.clone();
    }

    /**
     * The region whose postcode range contains {@code postcode}, or {@code null} when the
     * postcode is absent, not numeric, or in no known range.
     */
    public static String regionByPostcode(String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
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

    /**
     * The REGION component of an owner's identity, derived solely from the postcode: the region
     * whose range contains {@code postcode}, or {@link #UNKNOWN} when the postcode is absent or in
     * no known range. This one value is both the {@code <REGION>} prefix of the owner's
     * {@code memberId} and the owner's {@code locality}, so the two are always derived the same
     * way and can never diverge.
     */
    public static String identityRegion(String postcode) {
        String region = regionByPostcode(postcode);
        return region != null ? region : UNKNOWN;
    }

    /**
     * The version-2 REGION component used <em>inside</em> the owner's identifiers (the
     * {@code <REGION>} prefix of the {@code memberId}): the plain {@link #identityRegion(String)}
     * region with the fixed {@link IdentityVersion#TAG version tag} mixed in (e.g. {@code V2NSW},
     * {@code V2UNKNOWN}). Because a version-1 region code ({@code NSW}, {@code VIC}, {@code QLD},
     * {@code UNKNOWN}) can never carry the tag, no value produced under version 1 is produced again.
     *
     * <p>This is an identifier-only derivation: the user-facing {@code locality} and {@code timezone}
     * keep the plain {@link #identityRegion(String)} region, so the tag never leaks into them.
     */
    public static String identityRegionV2(String postcode) {
        return IdentityVersion.TAG + identityRegion(postcode);
    }

    /**
     * The IANA timezone for {@code region} (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane), or {@code null} when the region has no known timezone (including
     * {@link #UNKNOWN}).
     */
    public static String timezoneOf(String region) {
        return REGION_TIMEZONE.get(region);
    }
}
