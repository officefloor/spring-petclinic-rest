package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a create-owner request's telephone to E.164 form: a leading {@code '+'} and country code
 * are kept when present, otherwise country code {@code '+61'} is assumed and a single leading
 * {@code '0'} is dropped from the national digits; spaces, dashes and brackets are stripped and 8 to 15
 * digits are required after the {@code '+'}. So {@code '0412 345 678'} becomes {@code '+61412345678'}.
 * The normalized value is written back onto the request (the same object the later {@link BuildOwner}
 * step maps to the entity), so it is stored and returned as {@code telephone}. A number that cannot
 * form valid E.164 is rejected via {@link InvalidTelephoneException}, which the global handler turns
 * into a 400.
 */
public class NormalizeTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(TelephoneE164.normalize(request.getTelephone()));
    }
}
