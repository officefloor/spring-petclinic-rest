package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates an owner's postcode WHEN PRESENT. A postcode is optional: an absent (null)
 * postcode passes untouched, keeping the request contract backward-compatible. When
 * present it must be a 4-digit code and, if the city has a known region, fall within that
 * region's inclusive postcode range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a city
 * with no known region accepts any 4-digit postcode. Anything else is rejected 400 via
 * {@link InvalidPostcodeException}.
 */
public class ValidatePostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return; // optional when absent
        }
        if (!postcode.matches("^[0-9]{4}$")) {
            throw new InvalidPostcodeException("Postcode must be a 4-digit code");
        }
        // Region reference data lives once in OwnerCustomerCode; share it rather than copy it.
        String region = OwnerCustomerCode.regionForCity(request.getCity());
        int[] range = region == null ? null : OwnerCustomerCode.postcodeRange(region);
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(
                    "Postcode " + postcode + " is not valid for city " + request.getCity());
        }
    }
}
