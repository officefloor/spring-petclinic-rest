package org.springframework.samples.petclinic.model;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates a 4-digit postcode against the fixed region ranges derived from an owner's
 * city (see {@link Locality}): NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A city with
 * no known region accepts any 4-digit postcode.
 */
public final class Postcode {

    /** A postcode must be exactly four digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Postcode() {
    }

    /**
     * Whether {@code postcode} is valid for {@code city}: it must be exactly four digits and,
     * when the city maps to a known region, fall within that region's inclusive range. A city
     * with no known region accepts any 4-digit postcode.
     */
    public static boolean isValidForCity(String postcode, String city) {
        if (postcode == null || !FOUR_DIGITS.matcher(postcode).matches()) {
            return false;
        }
        int[] range = REGION_RANGE.get(Locality.of(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
