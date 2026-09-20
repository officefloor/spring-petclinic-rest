package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a create request's telephone by removing every non-digit character, then
 * requires exactly ten digits, storing the normalized value back on the body so later
 * steps persist and return it. Runs after {@link ValidateOwnerFields} has confirmed the
 * field is present and published the body; mutates that same instance in place.
 */
public class NormalizeOwnerTelephone {

    private static final int REQUIRED_DIGITS = 10;

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String digits = Telephones.normalize(telephone);
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(telephone);
        }
        request.setTelephone(digits);
    }
}
