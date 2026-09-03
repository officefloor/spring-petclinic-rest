package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the optional owner {@code postcode}. Postcode is absent when null or blank; when present it
 * must be a 4-digit value and, when the city maps to a known region, fall within that region's fixed
 * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit
 * postcode. The postcode is stored and returned as given (trimmed).
 */
final class Postcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** City -> canonical region, from the fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Postcode() {
    }

    /**
     * @return the trimmed postcode, or {@code null} when it is absent (null or blank).
     * @throws InvalidPostcodeException when present but not a 4-digit value, or out of range for the
     *         city's region.
     */
    static String normalize(String postcode, String city) throws InvalidPostcodeException {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String trimmed = postcode.trim();
        if (!FOUR_DIGITS.matcher(trimmed).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        String region = city == null ? null : CITY_REGION.get(city);
        if (region != null) {
            int[] range = REGION_RANGE.get(region);
            int value = Integer.parseInt(trimmed);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
        return trimmed;
    }
}
