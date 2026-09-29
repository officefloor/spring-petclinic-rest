package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs in the create-owner pipeline after {@link RequireOwnerFields}: converts the telephone to E.164
 * form (see {@link TelephoneNormalizer}). The E.164 value is written back onto the (shared)
 * {@link OwnerFieldsDto} so {@link BuildOwner} maps and stores it, and it is what
 * {@code GET /owners/{id}} later returns. A value that cannot form valid E.164 is rejected with a 400
 * by {@code InvalidTelephoneExceptionHandler}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto owner) throws InvalidTelephoneException {
        String e164 = TelephoneNormalizer.toE164(owner.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(owner.getTelephone());
        }
        owner.setTelephone(e164);
    }
}
