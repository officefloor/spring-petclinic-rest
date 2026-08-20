package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerTelephoneException;

/**
 * Normalizes the create-owner telephone into E.164 form: strips spaces, dashes and brackets,
 * keeps a leading '+' and country code when present, otherwise assumes country code '+61' and
 * drops a single leading '0' from the national digits, requiring 8 to 15 digits after the '+'.
 * The national-number length must also match the country code ('+61' → 9 national digits,
 * '+1' → 10). Mutates the published request in place so later steps store and return the E.164
 * value. Rejects anything that cannot form a valid E.164 number, or whose national-number length
 * is wrong for its country code, with a 400.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerTelephoneException {
        String e164 = TelephoneNormalizer.toE164(request.getTelephone());
        if (e164 == null || !TelephoneNormalizer.hasValidNationalLength(e164)) {
            throw new InvalidOwnerTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
    }
}
