package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Owner step: when the request carries a postcode, rejects it with a 400 unless it is a
 * 4-digit code valid for the city's region (see {@link Postcode}). Postcode is optional, so
 * a request without one is left untouched. Runs after the body-validating step (so the city
 * is available) and before the owner is loaded or built.
 */
public class EnsureValidPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        if (!Postcode.isWellFormed(postcode) || !Postcode.isInRegion(request.getCity(), postcode)) {
            throw new InvalidPostcodeException();
        }
    }
}
