package org.springframework.samples.petclinic.util;

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
 * The region is derived from the city via {@link LocalityResolver}, and the range reserved
 * for that region is sourced from {@link LocalityResolver#postcodeRange} (the single source
 * of truth for the region&harr;postcode relationship). A city with no known region (locality
 * {@code 'UNKNOWN'}) accepts any 4-digit postcode.
 */
public final class PostcodeValidator {

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
        int[] range = LocalityResolver.postcodeRange(LocalityResolver.resolve(city));
        if (range == null) {
            return;
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, city);
        }
    }
}
