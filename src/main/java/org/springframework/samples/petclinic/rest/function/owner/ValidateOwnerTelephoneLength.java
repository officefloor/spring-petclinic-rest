package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.TelephoneNationalNumberLengthException;
import org.springframework.samples.petclinic.util.E164PhoneNumber;

/**
 * Rejects an owner request whose (already E.164-normalized) telephone has a
 * national-number length that is wrong for its country code, with a 400. Runs after
 * {@link NormalizeOwnerTelephone} so it sees the canonical '+'-prefixed value.
 */
public class ValidateOwnerTelephoneLength {

    public void service(@Val OwnerFieldsDto request) throws TelephoneNationalNumberLengthException {
        if (!E164PhoneNumber.hasValidNationalNumberLength(request.getTelephone())) {
            throw new TelephoneNationalNumberLengthException(request.getTelephone());
        }
    }
}
