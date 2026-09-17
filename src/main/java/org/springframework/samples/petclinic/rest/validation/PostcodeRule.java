package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Decides whether an owner's postcode is valid for its city.
 *
 * <p>A postcode is always four digits. When present it must fall within the inclusive range fixed
 * for the region derived from the owner's city ({@code NSW 2000-2099}, {@code VIC 3000-3099},
 * {@code QLD 4000-4099}); the region is resolved via {@link LocalityResolver}. A city whose region
 * is unknown accepts any four-digit postcode.
 */
public final class PostcodeRule {

    /** A postcode is exactly four digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    /** Region -> inclusive four-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodeRule() {
    }

    /**
     * Resolve the region whose fixed postcode range contains {@code postcode}.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @return the canonical region string whose inclusive range contains the four-digit
     *         {@code postcode}, or {@code null} when the postcode is absent, malformed, or in no
     *         known range
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
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

    /**
     * Whether {@code postcode} is acceptable for {@code city}.
     *
     * @param city     the owner's city, possibly {@code null}
     * @param postcode the owner's postcode; {@code null} means "not supplied"
     * @return {@code true} when the postcode is absent, or is four digits within the range for the
     *         city's region, or is four digits and the city has no known region; {@code false} for a
     *         malformed postcode or one outside its region's range
     */
    public static boolean isValid(String city, String postcode) {
        if (postcode == null) {
            return true;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            return false;
        }
        int[] range = REGION_POSTCODES.get(LocalityResolver.resolve(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
