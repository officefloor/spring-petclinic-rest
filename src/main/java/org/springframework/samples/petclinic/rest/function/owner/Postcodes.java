package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.mapper.Localities;

/**
 * Validates an owner's optional postcode against its city's region.
 *
 * <p>A postcode, when supplied, must be exactly 4 digits and fall within the
 * inclusive range fixed for the region derived from the owner's city
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region
 * (locality {@code UNKNOWN}) accepts any 4-digit postcode.
 */
public final class Postcodes {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Postcodes() {
    }

    /**
     * Whether the postcode is acceptable for the given city. A blank/absent postcode
     * is acceptable (validated only when present). When present it must be 4 digits and,
     * for a city with a known region, within that region's range.
     */
    public static boolean isValid(String postcode, String city) {
        if (postcode == null || postcode.isBlank()) {
            return true;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            return false;
        }
        int[] range = REGION_RANGE.get(Localities.forCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }
}
