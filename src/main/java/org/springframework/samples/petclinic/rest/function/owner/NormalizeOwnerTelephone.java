package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a create request's telephone to canonical E.164 form, storing the result back
 * on the body so later steps persist and return it, and rejecting numbers that cannot form
 * valid E.164. Runs after {@link ValidateOwnerFields} has confirmed the field is present
 * and published the body; mutates that same instance in place.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String e164 = Telephones.toE164(telephone)
                .orElseThrow(() -> new InvalidTelephoneException(telephone));
        request.setTelephone(e164);
    }
}
