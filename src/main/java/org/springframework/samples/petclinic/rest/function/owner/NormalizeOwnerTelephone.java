package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone into E.164 form (see {@link E164Telephone}): a
 * leading '+' and country code are kept when present, otherwise country code '+61' is
 * assumed and a single leading '0' is dropped from the national digits; spaces, dashes and
 * brackets are stripped and the result must carry 8 to 15 digits after the '+'. Runs after
 * {@link RequireOwnerFields} (so the field is present) and before {@link BuildOwner},
 * mutating the published body in place so the mapped
 * {@link org.springframework.samples.petclinic.model.Owner} — and the response — carries
 * the E.164 value. A telephone that cannot form valid E.164 is rejected 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(E164Telephone.normalize(request.getTelephone()));
    }
}
