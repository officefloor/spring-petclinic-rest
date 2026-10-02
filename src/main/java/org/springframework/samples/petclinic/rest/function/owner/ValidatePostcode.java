package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the owner's {@code postcode} against its city, after the body has been published by
 * the preceding validation step. A postcode is optional: an owner created without one is accepted
 * unchanged. When present it must be 4 digits and, for a city with a known region, within that
 * region's inclusive range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); a city with no known
 * region accepts any 4-digit postcode. Rejects an out-of-range or malformed postcode with a 400.
 */
public class ValidatePostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (!OwnerPostcode.isValid(postcode, request.getCity())) {
            throw new InvalidPostcodeException(
                    "Postcode is not valid for city '" + request.getCity() + "': " + postcode);
        }
    }
}
