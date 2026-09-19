package org.springframework.samples.petclinic.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Postcode rules: the 4-digit format and the fixed per-region ranges (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). Regions are resolved from a city by
 * {@link CityLocality}; this class only knows regions, so the two concerns stay
 * separate and reusable.
 *
 * <p>A region with no known range accepts any well-formed postcode.
 */
public final class Postcodes {

    /** A postcode is exactly four decimal digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("\\d{4}");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Postcodes() {
    }

    /**
     * @param postcode the candidate postcode
     * @return {@code true} when {@code postcode} is exactly four digits
     */
    public static boolean hasValidFormat(String postcode) {
        return postcode != null && FOUR_DIGITS.matcher(postcode).matches();
    }

    /**
     * Whether a well-formed postcode is acceptable for a region. A region with no known
     * range accepts any postcode. Callers must first confirm the format with
     * {@link #hasValidFormat(String)}.
     *
     * @param region   the canonical region, as resolved by {@link CityLocality}
     * @param postcode a 4-digit postcode
     * @return {@code true} when the region has no range, or the postcode falls within it
     */
    public static boolean isAllowedForRegion(String region, String postcode) {
        int[] range = REGION_RANGE.get(region);
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * The canonical region whose range contains a postcode, the reverse of the
     * per-region ranges used by {@link #isAllowedForRegion(String, String)}.
     *
     * @param postcode the candidate postcode
     * @return the region containing {@code postcode}, or {@code null} when it is not
     *         well-formed or falls in no known range
     */
    public static String regionForPostcode(String postcode) {
        if (!hasValidFormat(postcode)) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
