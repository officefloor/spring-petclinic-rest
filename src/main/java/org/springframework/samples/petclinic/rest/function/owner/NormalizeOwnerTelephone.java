package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the submitted telephone on create by stripping every non-digit character,
 * then requires exactly 10 digits. The 10-digit value is written back onto the request
 * in place (the {@code @Val} object is shared with later steps) so downstream steps store
 * and return it; anything else is rejected with 400 via {@link InvalidTelephoneException}.
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
