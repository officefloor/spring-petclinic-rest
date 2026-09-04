package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Validates an owner's optional 4-digit {@code postcode} against the inclusive range
 * fixed for the owner's city region (see {@link Locality}): NSW 2000-2099, VIC
 * 3000-3099, QLD 4000-4099. A city with no known region accepts any 4-digit postcode,
 * and an absent postcode is always accepted.
 */
public final class Postcode {

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Postcode() {
    }

    /**
     * True when {@code postcode} is absent, the city's region is unknown, or the postcode
     * falls within that region's inclusive range. Assumes a non-null postcode is already
     * validated as four digits.
     */
    public static boolean isValidForCity(String city, String postcode) {
        if (postcode == null) {
            return true;
        }
        int[] range = REGION_RANGES.get(Locality.of(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
