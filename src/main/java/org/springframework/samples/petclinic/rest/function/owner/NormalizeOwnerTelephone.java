package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Second step of {@code POST /api/owners}: normalises the telephone by stripping every
 * non-digit character, then requires exactly 10 digits. The cleaned 10-digit value is written
 * back onto the (shared) request DTO so it is stored and returned as {@code telephone}; anything
 * that is not exactly 10 digits after stripping is rejected 400 via {@link InvalidTelephoneException}.
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
