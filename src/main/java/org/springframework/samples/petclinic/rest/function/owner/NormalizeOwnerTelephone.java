package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a create-owner telephone by removing every non-digit character, then requires
 * exactly 10 digits. Mutates the validated {@link OwnerFieldsDto} in place (so {@link BuildOwner}
 * stores the normalized value), or throws {@link InvalidTelephoneException} for a 400 when the
 * stripped value is not exactly 10 digits.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String original = request.getTelephone();
        String digits = original == null ? "" : original.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(original);
        }
        request.setTelephone(digits);
    }
}
