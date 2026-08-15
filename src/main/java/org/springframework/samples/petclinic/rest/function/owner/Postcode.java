package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.LocalityResolver;

/**
 * Shared validation for an owner's optional {@code postcode}. A postcode is only checked when
 * present (non-null, non-blank); an absent postcode is accepted so the request contract stays
 * backward-compatible. When present it must be exactly 4 digits and, for a city whose region is
 * known (see {@link LocalityResolver}), fall inside that region's fixed range: NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099. A city with no known region accepts any 4-digit postcode. A
 * malformed or out-of-range postcode is rejected with 400 via {@link InvalidPostcodeException}.
 */
final class Postcode {

    /** Exactly 4 digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("[0-9]{4}");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Postcode() {
    }

    /**
     * Validates {@code postcode} against the region derived from {@code city}. Does nothing when
     * the postcode is absent (null or blank).
     *
     * @throws InvalidPostcodeException when the postcode is malformed or out of range for the city.
     */
    static void validate(String postcode, String city) throws InvalidPostcodeException {
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_RANGE.get(LocalityResolver.localityOf(city));
        if (range == null) {
            return; // city has no known region: any 4-digit postcode is accepted
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }
}
