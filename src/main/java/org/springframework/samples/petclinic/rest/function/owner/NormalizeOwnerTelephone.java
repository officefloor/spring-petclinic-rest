package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a create-owner request's telephone to E.164 form via {@link OwnerTelephones}.
 * The E.164 value is stored back on the (already published) request so
 * {@link RejectDuplicateOwnerIdentity} and {@link BuildOwner} carry it through to
 * persistence. Runs after {@link ValidateRequiredOwnerFields}, so the telephone is known
 * non-blank here.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(OwnerTelephones.toE164(request.getTelephone()));
    }
}
