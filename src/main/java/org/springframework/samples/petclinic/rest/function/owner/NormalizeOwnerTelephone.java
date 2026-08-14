package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link ValidateOwnerFields} and before {@link BuildOwner} in the create-owner
 * pipeline. Normalizes the telephone by removing every non-digit character, requires exactly ten
 * digits, and mutates the validated body in place so later steps build and store the normalized
 * value. Rejects anything that is not exactly ten digits after stripping with a 400.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone(digits);
    }
}
