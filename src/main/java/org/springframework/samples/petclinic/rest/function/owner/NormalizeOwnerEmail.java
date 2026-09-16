package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the owner email when one is supplied: an absent (null or blank) email is
 * left untouched, otherwise it must be a syntactically valid address (else a 400 via
 * {@link InvalidEmailException}) and is stored lower-cased. Mutates the published body in
 * place so later steps (and the response) see the canonical value.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        String normalized = OwnerEmails.normalize(email);
        if (!OwnerEmails.isValid(normalized)) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(normalized);
    }
}
