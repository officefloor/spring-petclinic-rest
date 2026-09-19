package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone on a create request: strips every non-digit character, then
 * requires exactly ten digits. Stores the 10-digit value back on the request (mutating
 * the shared {@code @Val} object) so {@link BuildOwner} and the response carry it; rejects
 * with {@link InvalidTelephoneException} (400) otherwise. Runs after {@link ValidateNewOwner}
 * has confirmed the field is present.
 */
public class NormalizeOwnerTelephone {

    private static final int REQUIRED_DIGITS = 10;

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone(digits);
    }
}
