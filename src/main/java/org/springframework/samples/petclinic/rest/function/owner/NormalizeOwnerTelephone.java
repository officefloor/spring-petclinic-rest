package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone to E.164 form (see {@link TelephoneNormalizer}).
 * Runs after {@link ValidateOwnerFields} (so the field is known to be present) and
 * before {@link BuildOwner}, mutating the published {@link OwnerFieldsDto} in place so
 * the owner is built and stored with the E.164 value. A number that cannot form valid
 * E.164 is rejected as a 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String e164 = TelephoneNormalizer.toE164(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
    }
}
