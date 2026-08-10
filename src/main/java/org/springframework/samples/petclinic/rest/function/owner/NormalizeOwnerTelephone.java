package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after required-field validation and before {@link BuildOwner}. Normalizes the telephone by
 * removing every non-digit character, then requires exactly 10 digits. The normalized value is written
 * back onto the validated body (mutated in place via {@code @Val}) so downstream steps store and return
 * the 10-digit value. A telephone that is not exactly 10 digits after stripping is rejected by throwing
 * {@link InvalidTelephoneException} (handled as 400).
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(telephone);
        }
        request.setTelephone(digits);
    }
}
