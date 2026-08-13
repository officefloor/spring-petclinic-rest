package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link RequireOwnerFields} on {@code POST /api/owners}: strips every non-digit
 * character from the telephone and requires exactly ten digits, replacing the field in place
 * (so {@link BuildOwner} maps the normalized value). A telephone that is not exactly ten digits
 * after stripping is rejected with 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone(digits);
    }
}
