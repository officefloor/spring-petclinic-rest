package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Postcode validation for pet owners. A postcode is optional; when present it must be a 4-digit
 * number. When the owner's city maps to a known region (see {@link Locality}) the postcode must
 * fall inside that region's fixed inclusive range: {@code NSW 2000-2099}, {@code VIC 3000-3099},
 * {@code QLD 4000-4099}. A city with no known region ({@code UNKNOWN}) accepts any 4-digit
 * postcode. Derived purely from the fixed region table, so it is seed-independent.
 */
public final class Postcode {

    /** Region -> inclusive 4-digit postcode range {low, high}. Fixed, pinned reference data. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Postcode() {
    }

    /**
     * Validates the supplied postcode for the given city. A {@code null} or blank postcode is
     * treated as absent and passes (postcode is optional). Otherwise it must be exactly 4 digits,
     * and when the city's region has a known range the postcode must be within it.
     *
     * @throws InvalidPostcodeException when the postcode is malformed or out of range for the
     * city's region.
     */
    public static void validate(String postcode, String city) throws InvalidPostcodeException {
        if (postcode == null || postcode.isBlank()) {
            return; // absent: optional
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw new InvalidPostcodeException("Postcode must be 4 digits: " + postcode);
        }
        int[] range = REGION_RANGES.get(Locality.of(city));
        if (range == null) {
            return; // unknown region: any 4-digit postcode accepted
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException("Postcode " + postcode + " is not valid for city "
                    + city + " (expected " + range[0] + "-" + range[1] + ")");
        }
    }
}
