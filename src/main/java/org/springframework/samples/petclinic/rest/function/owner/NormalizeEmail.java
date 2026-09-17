package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional email of a create request: an absent or blank email is left untouched, while
 * a present one must be a syntactically valid address. The lower-cased form is written back onto the
 * (republished) request so {@link BuildOwner} stores and later responses return it lower-cased. An
 * invalid value is rejected as a 400 via {@link InvalidEmailException}.
 */
public class NormalizeEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = EmailNormalizer.normalize(email);
        if (!EmailNormalizer.isValid(normalized)) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(normalized);
    }
}
