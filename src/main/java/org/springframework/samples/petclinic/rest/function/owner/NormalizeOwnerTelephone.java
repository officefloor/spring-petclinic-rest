package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.util.E164PhoneNumber;

/**
 * Normalizes an owner request's telephone to E.164 form, rejecting anything that cannot
 * form a valid E.164 number with a 400. Runs after the request's presence has been
 * confirmed, and mutates the shared request in place so downstream steps
 * ({@link BuildOwner} / {@link ApplyOwner}) store, and the possible-duplicate check
 * ({@link AssignOwnerPossibleDuplicate}) compares, the E.164 value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String e164 = E164PhoneNumber.toE164(request.getTelephone())
                .orElseThrow(() -> new InvalidTelephoneException(request.getTelephone()));
        request.setTelephone(e164);
    }
}
