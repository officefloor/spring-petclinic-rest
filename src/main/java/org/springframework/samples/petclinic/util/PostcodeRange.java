package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Fixed inclusive 4-digit postcode ranges keyed by an owner's region (as derived by
 * {@link CityRegion}). The table is pinned; a region with no entry — including
 * {@link CityRegion#UNKNOWN} — accepts any well-formed 4-digit postcode.
 */
public final class PostcodeRange {

    /** Region to its inclusive {@code {low, high}} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodeRange() {
    }

    /**
     * Whether the given 4-digit {@code postcode} is valid for {@code city}. A city whose
     * region has no known range (the permissive default) accepts any postcode.
     *
     * @param city     the owner's city, mapped to a region via {@link CityRegion}
     * @param postcode a well-formed 4-digit postcode string
     * @return {@code true} when the postcode falls in the city's region range, or the
     *         region has no known range
     */
    public static boolean isValidForCity(String city, String postcode) {
        int[] range = REGION_RANGE.get(CityRegion.localityOf(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
