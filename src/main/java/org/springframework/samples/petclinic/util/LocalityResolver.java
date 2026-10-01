package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Derives an owner's 'locality' (canonical region) from the postcode and city.
 *
 * <p>The region is resolved by the postcode range first
 * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), and only
 * falls back to the fixed city-to-region table ({@code Sydney -> NSW},
 * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}) when the postcode is absent or
 * in no known range. Preferring the postcode returns the same region for known cities
 * but disambiguates cities that share a name.
 *
 * <p>A city not covered by either lookup (or a {@code null} city with no matching
 * postcode) resolves to {@code 'UNKNOWN'}.
 */
public final class LocalityResolver {

    /** City -> canonical region. Cities not listed derive locality 'UNKNOWN'. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {low, high}. This is the single source of
     * truth for the region&harr;postcode relationship, read both here (postcode -> region)
     * and by {@link PostcodeValidator} (region -> range, via {@link #postcodeRange}).
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Locality returned when neither the postcode nor the city resolves a region. */
    private static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Resolve the canonical region for a city, using the city-to-region table only.
     *
     * @param city the owner's city (any value)
     * @return the canonical region string, or {@code 'UNKNOWN'} when the city is not
     *         in the table.
     */
    public static String resolve(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Resolve the canonical region, preferring the postcode over the city. The region
     * is looked up by postcode range first; when the postcode is absent or in no known
     * range, the city-to-region table is used as a fallback.
     *
     * @param city     the owner's city (used as a fallback)
     * @param postcode the owner's postcode, or {@code null} when none was given
     * @return the canonical region string, or {@code 'UNKNOWN'} when neither lookup
     *         resolves a region.
     */
    public static String resolve(String city, String postcode) {
        String fromPostcode = resolveByPostcode(postcode);
        return fromPostcode != null ? fromPostcode : resolve(city);
    }

    /**
     * The inclusive 4-digit postcode range {@code {low, high}} reserved for a region, or
     * {@code null} when the region has no reserved range. Exposes the single-sourced
     * {@code REGION_POSTCODES} table for {@link PostcodeValidator}.
     *
     * @param region a canonical region string (e.g. {@code 'NSW'})
     * @return a defensive copy of the {@code {low, high}} range, or {@code null} if none
     */
    static int[] postcodeRange(String region) {
        int[] range = REGION_POSTCODES.get(region);
        return range == null ? null : range.clone();
    }

    /** Region whose range contains the postcode, or {@code null} if none (or unparseable). */
    private static String resolveByPostcode(String postcode) {
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
