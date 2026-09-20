package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a request whose supplied postcode is malformed or out of range for the owner's city
 * (see {@link Postcodes}). A postcode is optional: an absent postcode passes unchanged, keeping
 * the request contract backward-compatible.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!Postcodes.isValidForCity(postcode, request.getCity())) {
            throw new InvalidPostcodeException(postcode, request.getCity());
        }
    }
}
