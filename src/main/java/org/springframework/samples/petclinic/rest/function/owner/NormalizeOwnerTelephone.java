package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone by stripping every non-digit character, then
 * requires exactly ten digits. Runs after {@link ValidateOwnerFields} (so the field
 * is known to be present) and before {@link BuildOwner}, mutating the published
 * {@link OwnerFieldsDto} in place so the owner is built and stored with the ten-digit
 * value. A non-conforming number is rejected as a 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(digits);
    }
}
