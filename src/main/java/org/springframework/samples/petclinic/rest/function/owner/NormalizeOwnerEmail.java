package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a request's optional email: an absent (or blank) value is left untouched,
 * while a present value must be a syntactically valid address and is stored back lower-cased
 * so later steps persist and return the canonical form. Runs after the body has been
 * published; mutates that same instance in place. A present-but-invalid address is rejected
 * via {@link InvalidEmailException}.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!Emails.isValid(email)) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(Emails.normalize(email));
    }
}
