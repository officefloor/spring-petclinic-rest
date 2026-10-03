package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the submitted telephone on create into E.164 form via {@link OwnerTelephones}.
 * The E.164 value is written back onto the request in place (the {@code @Val} object is
 * shared with later steps) so downstream steps store and return it; a number that cannot
 * form valid E.164 is rejected with 400 via {@link InvalidTelephoneException}.
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
