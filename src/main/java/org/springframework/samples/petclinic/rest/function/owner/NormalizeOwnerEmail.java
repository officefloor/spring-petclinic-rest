package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes an owner request's optional email. When absent (null or blank) the field is
 * cleared and the step is a no-op; when present it must be a syntactically valid address,
 * otherwise a {@link InvalidEmailException} is thrown (400). A valid address is stored back on
 * the (already published) request lower-cased so later steps carry it through to persistence.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (!OwnerEmails.isPresent(email)) {
            request.setEmail(null);
            return;
        }
        String normalized = OwnerEmails.normalize(email);
        if (!OwnerEmails.isValid(normalized)) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        request.setEmail(normalized);
    }
}
