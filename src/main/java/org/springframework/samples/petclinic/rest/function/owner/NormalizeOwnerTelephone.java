package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Create-owner step: normalizes the telephone by removing every non-digit character and
 * requires exactly ten digits, rejecting anything else with a 400. Runs after
 * {@link ValidateOwnerFields} (which guarantees a non-blank value) and before
 * {@link BuildOwner}, mutating the published body in place so the stored and returned
 * telephone is the normalized ten-digit value.
 */
public class NormalizeOwnerTelephone {

    private static final int REQUIRED_DIGITS = 10;

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String digits = digitsOnly(request.getTelephone());
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException();
        }
        request.setTelephone(digits);
    }

    /** Strip every non-digit character, the shared definition of a normalized telephone. */
    static String digitsOnly(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
