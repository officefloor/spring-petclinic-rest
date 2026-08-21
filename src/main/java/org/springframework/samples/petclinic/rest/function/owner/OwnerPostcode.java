package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerPostcodeException;
import org.springframework.samples.petclinic.util.Locality;

/**
 * Shared postcode handling for the owner pipelines. An owner may include a {@code postcode}: it is
 * optional and validated only WHEN PRESENT, so an owner created without one is still accepted. When
 * present it must be four digits, and — if the owner's city maps to a known region — it must fall in
 * that region's fixed inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no
 * known region accepts any 4-digit postcode. Anything else is rejected with
 * {@link InvalidOwnerPostcodeException} (handled as 400). The trimmed postcode is stored and returned.
 */
final class OwnerPostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}; regions not listed accept any 4 digits. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerPostcode() {
    }

    /**
     * Validates and normalizes the postcode on the request in place: a present, non-blank postcode is
     * trimmed and checked against the four-digit and city-region rules; a null or blank postcode is
     * left unchanged (absent postcodes are accepted).
     */
    static void validate(OwnerFieldsDto request) throws InvalidOwnerPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String trimmed = postcode.trim();
        if (!FOUR_DIGITS.matcher(trimmed).matches()) {
            throw new InvalidOwnerPostcodeException(postcode);
        }
        int[] range = REGION_RANGE.get(Locality.of(request.getCity()));
        if (range != null) {
            int value = Integer.parseInt(trimmed);
            if (value < range[0] || value > range[1]) {
                throw new InvalidOwnerPostcodeException(postcode);
            }
        }
        request.setPostcode(trimmed);
    }
}
