package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone on a create request to E.164 form (see {@link TelephoneNormalizer}).
 * Stores the E.164 value back on the request (mutating the shared {@code @Val} object) so
 * {@link BuildOwner} and the response carry it; rejects with {@link InvalidTelephoneException}
 * (400) when the number cannot form valid E.164 or its national-number length is wrong for the
 * country code (see {@link E164CountryLength}). Runs after {@link ValidateNewOwner} has
 * confirmed the field is present.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String e164 = TelephoneNormalizer.toE164(raw);
        if (e164 == null || !E164CountryLength.isNationalLengthValid(e164)) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone(e164);
    }
}
