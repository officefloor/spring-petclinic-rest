package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a create-owner request's telephone by removing every non-digit character, then requires
 * exactly ten digits. The normalized value is written back onto the request (the same object the
 * later {@link BuildOwner} step maps to the entity), so it is stored and returned as {@code telephone}.
 * A telephone that is not exactly ten digits after stripping is rejected via
 * {@link InvalidTelephoneException}, which the global handler turns into a 400.
 */
public class NormalizeTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone(digits);
    }
}
