package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * The single definition of a valid owner postcode: a 4-digit code that, when the owner's
 * city has a known region (see {@link Locality}), must fall within that region's fixed
 * inclusive range. Cities with no known region accept any well-formed 4-digit postcode.
 *
 * <p>Lookup only; pure functions of the postcode and city.
 */
public final class Postcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE =
            Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099},
                    "QLD", new int[] {4000, 4099});

    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    private Postcode() {
    }

    /** Whether {@code postcode} is a well-formed 4-digit code. */
    public static boolean isWellFormed(String postcode) {
        return postcode != null && FOUR_DIGITS.matcher(postcode).matches();
    }

    /** Whether a well-formed 4-digit {@code postcode} is permitted for {@code city}: within the
     *  city's region range, or anything when the city has no known region. */
    public static boolean isInRegion(String city, String postcode) {
        int[] range = REGION_RANGE.get(Locality.region(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /** The region whose fixed range contains {@code postcode}, or {@code null} when the postcode
     *  is absent, malformed, or falls in no known range. */
    public static String region(String postcode) {
        if (!isWellFormed(postcode)) {
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
