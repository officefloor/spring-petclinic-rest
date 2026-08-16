package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;

/**
 * Normalizes the telephone of the validated body (published by {@link RequireOwnerFields})
 * by stripping every non-digit character, then requires exactly 10 digits. The stripped,
 * 10-digit value is written back onto the body in place, so {@link BuildOwner} maps the
 * normalized number and later reads/responses return it. A telephone that is not exactly
 * 10 digits after stripping is rejected 400 via {@link OwnerTelephoneInvalidException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws OwnerTelephoneInvalidException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new OwnerTelephoneInvalidException(digits);
        }
        request.setTelephone(digits);
    }
}
