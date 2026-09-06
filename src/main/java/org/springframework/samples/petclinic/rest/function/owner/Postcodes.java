package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates an owner's optional 4-digit postcode against the fixed region ranges,
 * keyed by the city's region (Sydney->NSW, Melbourne->VIC, Brisbane->QLD): NSW
 * 2000-2099, VIC 3000-3099, QLD 4000-4099. A city with no known region accepts any
 * 4-digit postcode. Validation is skipped when no postcode is supplied.
 */
final class Postcodes {

    private Postcodes() {
    }

    /** Fixed city-to-region table (mirrors the read-only {@code locality} mapping). */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /**
     * Validate the supplied postcode for the given city. A blank/absent postcode is a
     * no-op (postcode is optional). A city with no known region accepts any 4-digit
     * postcode. When the city's region is known, the postcode must fall within that
     * region's inclusive range, otherwise an {@link InvalidPostcodeException} is thrown.
     */
    static void validate(String city, String postcode) throws InvalidPostcodeException {
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String region = CITY_REGION.get(city);
        if (region == null) {
            return; // unknown region accepts any 4-digit postcode
        }
        int[] range = REGION_POSTCODES.get(region);
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            throw new InvalidPostcodeException(
                    "Postcode '" + postcode + "' must be four digits");
        }
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException("Postcode '" + postcode + "' is not valid for "
                    + city + " (" + region + " expects " + range[0] + "-" + range[1] + ")");
        }
    }
}
