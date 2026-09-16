package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneLengthException;

/**
 * Validates the already-canonicalized E.164 telephone's national-number length against its
 * country calling code (e.g. {@code +61} requires 9 national digits, {@code +1} requires
 * 10), rejecting a wrong length with a 400 via {@link InvalidTelephoneLengthException}.
 * Runs after {@link NormalizeOwnerTelephone}, which has already put the body's telephone in
 * E.164 form.
 */
public class RequireTelephoneNationalLength {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneLengthException {
        String telephone = request.getTelephone();
        if (!OwnerTelephones.hasValidNationalLength(telephone)) {
            throw new InvalidTelephoneLengthException(telephone);
        }
    }
}
