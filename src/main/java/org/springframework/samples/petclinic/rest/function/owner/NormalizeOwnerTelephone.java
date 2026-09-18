package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone of a create-owner request in place: strips every non-digit
 * character, then requires exactly ten digits. Runs after {@link ValidateRequiredOwnerFields}
 * (so the field is known present) and before {@link BuildOwner} maps the DTO, so the entity
 * is built and stored with the cleaned 10-digit value. Rejects with 400 otherwise.
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
