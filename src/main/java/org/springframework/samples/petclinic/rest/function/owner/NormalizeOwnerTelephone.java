package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone of a create-owner request to E.164 form: strips spaces, dashes and brackets,
 * keeps a leading '+' and country code when present, otherwise assumes country code '+61' and drops a
 * single leading '0' from the national digits, requiring 8 to 15 digits after the '+'. Anything that
 * cannot form a valid E.164 number is rejected with a 400 via {@link InvalidTelephoneException}. Mutates
 * the validated body in place (via {@code @Val}), so the stored and returned {@code telephone} is the
 * E.164 string (e.g. {@code 0412 345 678} becomes {@code +61412345678}). Runs after
 * {@link ValidateOwnerFields} (which guarantees a non-blank telephone) and before {@link BuildOwner}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(E164Telephone.normalize(request.getTelephone()));
    }
}
