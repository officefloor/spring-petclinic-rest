package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Validates the create-owner telephone's national-number length against its country code:
 * country code '+61' (Australia) requires exactly 9 national digits and '+1' (NANP) requires
 * exactly 10 (see {@link E164Telephone#validateNationalNumberLength}). Runs after
 * {@link NormalizeOwnerTelephone} (so the published body already carries the E.164 value) and
 * before the duplicate checks, so a number whose national length is wrong for its country is
 * rejected 400 via {@link InvalidTelephoneException} rather than persisted.
 */
public class ValidateTelephoneCountryLength {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        E164Telephone.validateNationalNumberLength(request.getTelephone());
    }
}
