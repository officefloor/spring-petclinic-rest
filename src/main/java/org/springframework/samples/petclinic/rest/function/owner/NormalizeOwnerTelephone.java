package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Canonicalizes the create-owner telephone to E.164 form, rejecting anything that cannot
 * form a valid E.164 number with a 400 via {@link InvalidTelephoneException}. Mutates the
 * published body in place so later steps (and the response) see the E.164 value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String e164 = OwnerTelephones.toE164(telephone);
        if (e164 == null) {
            throw new InvalidTelephoneException(telephone);
        }
        request.setTelephone(e164);
    }
}
