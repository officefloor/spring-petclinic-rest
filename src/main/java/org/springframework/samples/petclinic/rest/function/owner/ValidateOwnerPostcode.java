package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the submitted postcode against the owner's city region, but only WHEN
 * PRESENT: an absent postcode is left untouched so an owner created without one is still
 * accepted (the request contract stays backward-compatible). The 4-digit format is
 * enforced by the DTO {@code @Pattern}; this step adds the range rule via
 * {@link OwnerPostcodes}, rejecting an out-of-range postcode with 400 through
 * {@link InvalidPostcodeException}.
 */
public class ValidateOwnerPostcode {

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null) {
            return;
        }
        if (!OwnerPostcodes.isValidForCity(postcode, request.getCity())) {
            throw new InvalidPostcodeException(postcode, request.getCity(),
                    Owner.regionForCity(request.getCity()));
        }
    }
}
