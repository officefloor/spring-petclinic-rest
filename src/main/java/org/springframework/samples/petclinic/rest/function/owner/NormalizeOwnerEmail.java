package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;

/**
 * Normalizes an owner's optional {@code email}. When the body carries no email the step
 * does nothing, so email stays optional. When one is present it must be a syntactically
 * valid address, otherwise {@link InvalidOwnerFieldsException} yields a 400; a valid
 * address is lower-cased in place on the shared body so it is persisted and returned
 * lower-cased. It mutates the already-published body variable rather than binding
 * {@code @RequestBody}, so it composes after the sole binding step in each pipeline.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerFieldsException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        if (!OwnerEmail.isValid(email)) {
            throw new InvalidOwnerFieldsException(List.of("email"));
        }
        request.setEmail(OwnerEmail.normalize(email));
    }
}
