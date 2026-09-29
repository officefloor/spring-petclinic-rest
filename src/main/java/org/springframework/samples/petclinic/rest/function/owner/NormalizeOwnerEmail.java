package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional owner {@code email}: a blank (absent) value is left as-is, a present value
 * must be a syntactically valid address and is stored lower-cased. The normalized value is written back
 * onto the (shared) {@link OwnerFieldsDto} so {@link BuildOwner}/{@link ApplyOwner} store it and
 * {@code GET /owners/{id}} later returns it. A present-but-invalid value is rejected with a 400 by
 * {@code InvalidEmailExceptionHandler}.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto owner) throws InvalidEmailException {
        String email = owner.getEmail();
        if (EmailNormalizer.isBlank(email)) {
            return;
        }
        if (!EmailNormalizer.isValid(email)) {
            throw new InvalidEmailException(email);
        }
        owner.setEmail(EmailNormalizer.normalize(email));
    }
}
