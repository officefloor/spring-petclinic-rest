package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Validates an optional postcode on an owner request. Postcode is not required: a null or
 * blank value is left untouched. When present it must be a 4-digit value valid for the owner's
 * city region (see {@link Postcodes}), otherwise the request is rejected with
 * {@link InvalidPostcodeException} (400).
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!Postcodes.isValidForCity(request.getCity(), postcode)) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
    }
}
