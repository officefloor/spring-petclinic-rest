package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone by stripping every non-digit character, then
 * requires exactly ten digits, rejecting anything else with a 400 via
 * {@link InvalidTelephoneException}. Mutates the published body in place so later steps
 * (and the response) see the 10-digit value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String digits = OwnerTelephones.normalize(telephone);
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(telephone);
        }
        request.setTelephone(digits);
    }
}
