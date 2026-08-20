package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a create-owner telephone into E.164 form: a leading '+' and country code are kept
 * when present, otherwise country code '+61' is assumed and a single leading '0' is dropped from
 * the national digits. Spaces, dashes and brackets are stripped, and 8 to 15 digits are required
 * after the '+'. The national-number length must also match the country code ('+61' requires 9
 * national digits, '+1' requires 10). Mutates the validated {@link OwnerFieldsDto} in place (so
 * {@link BuildOwner} stores the E.164 value), or throws {@link InvalidTelephoneException} for a 400
 * when the value cannot form a valid E.164 number or its national length is wrong for the country.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String original = request.getTelephone();
        String e164 = TelephoneE164.toE164(original);
        if (e164 == null || !TelephoneE164.nationalLengthValid(e164)) {
            throw new InvalidTelephoneException(original);
        }
        request.setTelephone(e164);
    }
}
