package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes an owner request's telephone in place into E.164 form (see {@link E164Telephone}).
 * Runs after the field is known present and before the DTO is applied to an entity, so owner
 * telephones are stored, returned and duplicate-checked as their E.164 value. Rejects with 400
 * when no valid E.164 number can be formed.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String e164 = E164Telephone.format(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
    }
}
