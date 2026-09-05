package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone by removing every non-digit character, then
 * requires exactly 10 digits. Runs after {@link RequireOwnerFields} (so the field is
 * present) and before {@link BuildOwner}, mutating the published body in place so the
 * mapped {@link org.springframework.samples.petclinic.model.Owner} — and the response —
 * carries the stripped 10-digit value. A telephone that is not exactly 10 digits after
 * stripping is rejected 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(
                    "Telephone must be exactly 10 digits after removing non-digit characters");
        }
        request.setTelephone(digits);
    }
}
