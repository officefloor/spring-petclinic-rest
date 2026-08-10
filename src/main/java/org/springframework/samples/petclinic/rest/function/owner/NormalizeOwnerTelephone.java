package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after required-field validation and before {@link BuildOwner}. Normalizes the telephone into
 * E.164 form: spaces, dashes and brackets are stripped; a leading {@code '+'} and its country code are
 * kept when present, otherwise country code {@code '+61'} is assumed and a single leading {@code '0'} is
 * dropped from the national digits. The result must carry 8 to 15 digits after the {@code '+'}. So
 * {@code '0412 345 678'} becomes {@code '+61412345678'}. The E.164 value is written back onto the
 * validated body (mutated in place via {@code @Val}) so downstream steps store and return it. A
 * telephone that cannot form valid E.164 is rejected by throwing {@link InvalidTelephoneException}
 * (handled as 400).
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(TelephoneE164.toE164(request.getTelephone()));
    }
}
