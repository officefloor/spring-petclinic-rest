package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Second step of {@code POST /api/owners}: normalises the telephone to E.164 form (strip spaces,
 * dashes and brackets; keep a leading {@code '+'} and country code, else assume {@code '+61'} and
 * drop a single leading {@code '0'}; require 8 to 15 digits after the {@code '+'}, and the correct
 * national-number length for a known country code &mdash; {@code +61} needs 9 national digits,
 * {@code +1} needs 10). The E.164 value is written back onto the (shared) request DTO so it is
 * stored and returned as {@code telephone}; anything that cannot form valid E.164 is rejected 400
 * via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String e164 = E164Telephone.normalize(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
    }
}
