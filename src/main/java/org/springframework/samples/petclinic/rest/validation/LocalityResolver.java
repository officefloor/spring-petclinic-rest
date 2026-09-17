package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region), preferring the postcode over the city.
 *
 * <p>The postcode is looked up first via {@link PostcodeRule#regionForPostcode(String)}
 * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}); the matching canonical
 * region string is returned. When the postcode is absent or in no known range, the city is looked
 * up in a small, fixed city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
 * {@code Brisbane -> QLD}). Any city not present in the table — including a {@code null} city —
 * derives the sentinel locality {@code "UNKNOWN"}. Preferring the postcode returns the same region
 * for known cities but disambiguates cities that share a name.
 */
public final class LocalityResolver {

    /** Locality returned for any city not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region; anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    private LocalityResolver() {
    }

    /**
     * Resolve the canonical region, preferring the postcode over the city.
     *
     * @param city     the owner's city, possibly {@code null}
     * @param postcode the owner's postcode, possibly {@code null}
     * @return the region whose range contains the postcode; otherwise the region derived from the
     *         city via {@link #resolve(String)} (which yields {@code "UNKNOWN"} for an unknown city)
     */
    public static String resolve(String city, String postcode) {
        String region = PostcodeRule.regionForPostcode(postcode);
        return region != null ? region : resolve(city);
    }

    /**
     * Resolve the canonical region from the postcode alone.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @return the region whose fixed range contains the postcode via
     *         {@link PostcodeRule#regionForPostcode(String)}, or {@link #UNKNOWN} when the postcode
     *         is absent, malformed, or in no known range
     */
    public static String resolveFromPostcode(String postcode) {
        String region = PostcodeRule.regionForPostcode(postcode);
        return region != null ? region : UNKNOWN;
    }

    /**
     * Resolve the canonical region for a city alone.
     *
     * @param city the owner's city, possibly {@code null}
     * @return the canonical region string when the city is in the table, otherwise {@code "UNKNOWN"}
     */
    public static String resolve(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Whether {@code region} is a known canonical region (NSW, VIC or QLD) rather than the
     * {@link #UNKNOWN} sentinel or {@code null}.
     *
     * @param region a canonical region string, possibly {@code null}
     * @return {@code true} when the region is a known region, {@code false} otherwise
     */
    public static boolean isKnownRegion(String region) {
        return region != null && !UNKNOWN.equals(region);
    }
}
