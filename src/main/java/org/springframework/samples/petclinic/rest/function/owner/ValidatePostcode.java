package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Validates an owner request's optional {@code postcode}. When absent (null or blank) the request is
 * accepted unchanged, keeping the create contract backward-compatible. When present the postcode must be
 * exactly four digits and, for a city whose region is known (via {@link Locality}), must fall within that
 * region's range (see {@link PostcodeRanges}: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no
 * known region accepts any 4-digit postcode. A malformed or out-of-range postcode is rejected via
 * {@link InvalidPostcodeException}, which the global handler turns into a 400.
 */
public class ValidatePostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // postcode is optional; nothing to validate when absent
        }
        if (!postcode.matches("[0-9]{4}")) {
            throw InvalidPostcodeException.malformed(postcode);
        }
        String region = Locality.of(request.getCity());
        int[] range = PostcodeRanges.forRegion(region);
        if (range == null) {
            return; // no known region for this city -> any 4-digit postcode is accepted
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw InvalidPostcodeException.outOfRange(postcode, request.getCity(), region, range);
        }
    }
}
