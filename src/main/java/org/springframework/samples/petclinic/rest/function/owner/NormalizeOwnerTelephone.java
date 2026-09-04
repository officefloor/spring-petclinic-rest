package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone on create into E.164 form (see {@link E164Telephone}). Mutates the
 * validated {@link OwnerFieldsDto} in place so {@link BuildOwner} maps the stored E.164 value, and
 * rejects anything that cannot form a valid number with 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(E164Telephone.toE164(request.getTelephone()));
    }
}
