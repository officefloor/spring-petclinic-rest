package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Fixed region-to-postcode-range policy. A 4-digit postcode is valid for a city when it
 * falls within the inclusive range of the city's {@link CityRegion region} (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). A city whose region is {@link CityRegion#UNKNOWN unknown}
 * accepts any 4-digit postcode.
 */
public final class PostcodeRange {

    /** The pinned region-to-inclusive-range table. */
    private static final Map<String, int[]> BY_REGION = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private PostcodeRange() {
    }

    /**
     * The region whose inclusive range contains {@code postcode}, or {@code null} when the
     * postcode is absent, non-numeric or in no known range. This is the reverse of the
     * {@link #BY_REGION region-to-range table} and the primary source of an owner's region.
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : BY_REGION.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether the given 4-digit {@code postcode} is valid for {@code city}: within the
     * inclusive range of the city's region, or unconstrained when the region is unknown.
     * Assumes {@code postcode} is a 4-digit numeric string (as enforced by the DTO pattern).
     */
    public static boolean isValidForCity(String city, String postcode) {
        int[] range = BY_REGION.get(CityRegion.locality(city));
        if (range == null) {
            return true; // no known region -> any 4-digit postcode is accepted
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
