package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a create-owner request's telephone by stripping every non-digit character,
 * requiring the result to be exactly ten digits. The stripped value is stored back on the
 * (already published) request so {@link BuildOwner} carries it through to persistence.
 * Runs after {@link ValidateRequiredOwnerFields}, so the telephone is known non-blank here.
 */
public class NormalizeOwnerTelephone {

    private static final int REQUIRED_DIGITS = 10;

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(
                    "Telephone must be exactly " + REQUIRED_DIGITS + " digits after removing non-digit characters");
        }
        request.setTelephone(digits);
    }
}
