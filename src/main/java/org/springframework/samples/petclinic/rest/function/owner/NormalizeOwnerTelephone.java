package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs in the create-owner pipeline after {@link RequireOwnerFields}: normalizes the telephone by
 * removing every non-digit character, then requires exactly 10 digits. The normalized 10-digit value
 * is written back onto the (shared) {@link OwnerFieldsDto} so {@link BuildOwner} maps and stores it,
 * and it is what {@code GET /owners/{id}} later returns. A value that is not exactly 10 digits after
 * stripping is rejected with a 400 by {@code InvalidTelephoneExceptionHandler}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto owner) throws InvalidTelephoneException {
        String digits = TelephoneNormalizer.normalize(owner.getTelephone());
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(digits);
        }
        owner.setTelephone(digits);
    }
}
