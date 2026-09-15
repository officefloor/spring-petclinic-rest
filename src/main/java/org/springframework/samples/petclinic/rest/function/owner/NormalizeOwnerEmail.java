package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional owner email. Email is optional, so a null or blank value is
 * left untouched; when present it must be a syntactically valid address (otherwise a
 * 400 via {@link InvalidEmailException}) and is stored lower-cased. Mutates the
 * published {@link OwnerFieldsDto} in place so the owner is built and saved with the
 * normalized value.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EmailNormalizer.isValid(email)) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(EmailNormalizer.normalize(email));
    }
}
