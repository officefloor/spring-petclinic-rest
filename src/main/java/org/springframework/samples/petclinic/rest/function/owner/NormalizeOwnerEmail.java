package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time email handling for {@code POST /owners}. Email is optional: an absent (null or
 * blank) value is left untouched and the request proceeds. When a value is supplied it must be
 * a syntactically valid address — otherwise the request is rejected with 400 via
 * {@link InvalidEmailException}. A valid value is normalised (trimmed and lower-cased) and
 * written back onto the body in place (the {@code @Val} object is the same instance
 * {@link BuildOwner} then maps), so the lower-cased form is what gets stored and returned.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} and before {@link CheckOwnerIdentityUnique}.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (!OwnerEmail.isPresent(email)) {
            return;
        }
        if (!OwnerEmail.isValid(email)) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(OwnerEmail.normalize(email));
    }
}
