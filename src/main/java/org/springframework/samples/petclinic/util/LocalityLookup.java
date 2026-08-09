package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's locality (region) from their postcode, falling back to their city.
 *
 * <p>The postcode is matched against fixed 4-digit ranges (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). When the postcode is absent or falls in no known range, the region is looked up
 * in a fixed city-to-region table that pins Sydney -&gt; NSW, Melbourne -&gt; VIC and
 * Brisbane -&gt; QLD. Anything that matches neither derives the locality {@code "UNKNOWN"}.
 */
public final class LocalityLookup {

    /** City -&gt; canonical region; anything not listed derives locality {@code "UNKNOWN"}. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES =
        Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Region -&gt; IANA timezone; anything not listed has no timezone. */
    private static final Map<String, String> REGION_TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private LocalityLookup() {
    }

    /**
     * Returns an owner's user-facing locality (region), derived purely from the stored postcode and
     * city (see {@link #forPostcodeAndCity(String, String)}).
     *
     * <p>The locality is the <em>plain</em> region code (for example {@code "NSW"}); it is not an
     * identifier and never carries the identity version tag that the version-2 identifiers embed, so it
     * is derived directly from the postcode and city rather than read back out of the member id.
     *
     * @param postcode the owner's stored postcode, or {@code null}
     * @param city the owner's stored city, or {@code null}
     * @return the plain region code, or {@code "UNKNOWN"} when neither postcode nor city maps
     */
    public static String locality(String postcode, String city) {
        return forPostcodeAndCity(postcode, city);
    }

    /**
     * Returns the IANA timezone name for the owner's plain locality (region) derived from the stored
     * postcode and city (see {@link #locality(String, String)}).
     *
     * <p>The derived region is mapped through the fixed region-to-timezone table
     * (NSW -&gt; {@code Australia/Sydney}, VIC -&gt; {@code Australia/Melbourne},
     * QLD -&gt; {@code Australia/Brisbane}). Regions not in the table (including {@code "UNKNOWN"})
     * have no timezone and yield {@code null}.
     *
     * @param postcode the owner's stored postcode, or {@code null}
     * @param city the owner's stored city, or {@code null}
     * @return the IANA timezone name for the derived region, or {@code null} when it has none
     */
    public static String timezone(String postcode, String city) {
        return REGION_TIMEZONE.get(locality(postcode, city));
    }

    /**
     * Returns the owner's segment, formatted {@code <TIER>_<AREA>}, one of {@code PREMIUM_METRO},
     * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
     *
     * <p>TIER is {@code PREMIUM} when the given membership level is 3 or more, otherwise
     * {@code STANDARD}. AREA is {@code METRO} when the owner's plain locality derived from the stored
     * postcode and city (see {@link #locality(String, String)}) is a known region (NSW, VIC or QLD),
     * otherwise {@code REGIONAL}. The segment's derived region is always the plain region code; it never
     * carries the version-2 identity tag.
     *
     * @param membershipLevel the owner's derived membership level, or {@code null}
     * @param postcode the owner's stored postcode, or {@code null}
     * @param city the owner's stored city, or {@code null}
     * @return the formatted owner segment
     */
    public static String ownerSegment(Integer membershipLevel, String postcode, String city) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String locality = locality(postcode, city);
        String area = REGION_POSTCODES.containsKey(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Returns the canonical region for the given postcode and city, preferring the postcode.
     *
     * <p>The postcode is looked up by range first; only when it is absent or in no known range does
     * the city-to-region table decide. This returns the same region for known cities but
     * disambiguates cities that share a name.
     *
     * @param postcode the owner's stored postcode, or {@code null}
     * @param city the owner's stored city, or {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when neither postcode nor city maps
     */
    public static String forPostcodeAndCity(String postcode, String city) {
        String region = forPostcode(postcode);
        return region != null ? region : forCity(city);
    }

    /**
     * Returns the canonical region for the given city, or {@code "UNKNOWN"} when the city is not in
     * the fixed city-to-region table (including a {@code null} city).
     *
     * @param city the owner's stored city, or {@code null}
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String forCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Returns the canonical region whose postcode range contains the given postcode, or
     * {@code null} when the postcode is absent, non-numeric, or in no known range.
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
