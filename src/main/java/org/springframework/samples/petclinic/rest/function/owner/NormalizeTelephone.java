package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone of a create request to canonical E.164 form (see
 * {@link TelephoneNormalizer#toE164(String)}). The normalized value is written back onto the
 * (republished) request so {@link BuildOwner} stores and later responses return the E.164 form. A
 * value that cannot form a valid E.164 number is rejected as a 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String e164 = TelephoneNormalizer.toE164(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
    }
}
