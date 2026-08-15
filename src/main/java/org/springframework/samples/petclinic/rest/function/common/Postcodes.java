package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Fixed region-to-postcode-range lookup used to validate an owner's optional 4-digit
 * 'postcode' against the region derived from its city (see {@link Localities}). The ranges
 * are pinned and inclusive: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099. A city whose region
 * is not in the table (locality "UNKNOWN") accepts any 4-digit postcode.
 */
public final class Postcodes {

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Postcodes() {
    }

    /**
     * The region whose inclusive range contains {@code postcode}, or {@code null} when the
     * postcode is absent, cannot be parsed as an integer, or falls in no known range. Used to
     * derive the locality from the postcode in preference to the city (see {@link Localities}).
     */
    public static String regionOf(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether {@code postcode} is acceptable for {@code city}. A null postcode is acceptable
     * (the field is optional). Assumes the 4-digit format has already been validated; a value
     * that cannot be parsed as an integer is treated as invalid. When the city's region has no
     * known range every 4-digit postcode is accepted.
     */
    public static boolean isValidForCity(String postcode, String city) {
        if (postcode == null) {
            return true;
        }
        int[] range = REGION_RANGE.get(Localities.of(city));
        if (range == null) {
            return true;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        }
        catch (NumberFormatException ex) {
            return false;
        }
        return value >= range[0] && value <= range[1];
    }
}
