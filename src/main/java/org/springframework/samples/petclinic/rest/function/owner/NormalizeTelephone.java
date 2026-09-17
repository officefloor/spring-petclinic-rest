package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone of a create request by stripping every non-digit character, then requires
 * exactly ten digits. The normalized value is written back onto the (republished) request so
 * {@link BuildOwner} stores and later responses return the digits-only form. A value that is not
 * exactly ten digits after stripping is rejected as a 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(digits);
    }
}
