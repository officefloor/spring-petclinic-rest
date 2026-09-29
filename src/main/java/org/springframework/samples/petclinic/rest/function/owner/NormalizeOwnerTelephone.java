package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone on create to E.164 form: strips spaces, dashes and brackets;
 * keeps a leading '+' and country code when present, otherwise assumes country code '+61'
 * and drops a single leading '0' from the national digits; requires 8 to 15 digits after
 * the '+'. The E.164 string is stored back on the request so later steps build, save and
 * return it. A value that cannot form valid E.164 is rejected 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String e164 = OwnerTelephones.toE164(raw);
        if (e164 == null) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone(e164);
    }
}
