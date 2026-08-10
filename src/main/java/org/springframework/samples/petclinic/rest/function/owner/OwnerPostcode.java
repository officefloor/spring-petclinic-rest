package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.Localities;

/**
 * Shared postcode validation for the owner create/update pipelines. Postcode is optional; when a
 * request carries one it must be exactly 4 digits and, for a city with a known region, fall within
 * that region's fixed range. A city with no known region (locality {@code "UNKNOWN"}) accepts any
 * 4-digit postcode. The postcode is stored and returned exactly as given.
 */
final class OwnerPostcode {

    /** A postcode must be exactly 4 digits. */
    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private OwnerPostcode() {
    }

    /**
     * Validates the postcode on the request. An absent (null) postcode is left untouched. A present
     * postcode must be 4 digits and, when the city's region is known, within that region's range.
     *
     * @throws InvalidPostcodeException when a postcode is present but malformed or out of range for
     *         the city's region.
     */
    static void validate(OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        int[] range = REGION_RANGE.get(Localities.localityFor(request.getCity()));
        if (range == null) {
            return; // no known region: any 4-digit postcode is accepted
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode);
        }
    }
}
