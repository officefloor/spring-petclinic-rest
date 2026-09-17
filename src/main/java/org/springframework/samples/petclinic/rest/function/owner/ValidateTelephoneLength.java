package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneLengthException;

/**
 * Checks that the already-normalized E.164 telephone has the national-number length its country code
 * requires (see {@link TelephoneNormalizer#hasValidNationalLength(String)}). Runs after
 * {@link NormalizeTelephone} so it judges the canonical '+' form. A wrong length is rejected as a 400
 * via {@link InvalidTelephoneLengthException}.
 */
public class ValidateTelephoneLength {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneLengthException {
        if (!TelephoneNormalizer.hasValidNationalLength(request.getTelephone())) {
            throw new InvalidTelephoneLengthException(request.getTelephone());
        }
    }
}
