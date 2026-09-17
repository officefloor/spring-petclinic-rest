package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Create-owner step: rewrites the telephone into canonical E.164 form (see
 * {@link E164Telephone}), rejecting anything that cannot form a valid number with a 400.
 * Runs after {@link ValidateOwnerFields} (which guarantees a non-blank value) and before
 * {@link BuildOwner}, mutating the published body in place so the stored and returned
 * telephone is the E.164 string.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(E164Telephone.normalize(request.getTelephone()));
    }
}
