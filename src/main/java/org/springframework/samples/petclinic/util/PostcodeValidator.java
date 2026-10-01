package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Validates an owner's 'postcode' against the owner's city.
 *
 * <p>A postcode is validated ONLY when present; a {@code null} postcode is always accepted,
 * keeping the request contract backward-compatible. When present it is expected to be four
 * digits (enforced by the request schema) and must fall within the inclusive range reserved
 * for the city's region per the fixed table:
 * <ul>
 *   <li>NSW: 2000-2099</li>
 *   <li>VIC: 3000-3099</li>
 *   <li>QLD: 4000-4099</li>
 * </ul>
 * The region is derived from the city via {@link LocalityResolver}. A city with no known
 * region (locality {@code 'UNKNOWN'}) accepts any 4-digit postcode.
 */
public final class PostcodeValidator {

    /** Region -> inclusive 4-digit postcode range {low, high}. Regions not listed accept any postcode. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private PostcodeValidator() {
    }

    /**
     * Validate a postcode for a city, throwing when the postcode is out of range for the
     * city's region.
     *
     * @param city     the owner's city (used to derive the region)
     * @param postcode the supplied postcode, or {@code null} when none was given
     * @throws InvalidPostcodeException if a 4-digit postcode is present but falls outside the
     *         inclusive range reserved for the city's region.
     */
    public static void validate(String city, String postcode) {
        if (postcode == null) {
            return;
        }
        int[] range = REGION_POSTCODES.get(LocalityResolver.resolve(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, city);
        }
    }
}
