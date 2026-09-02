package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerEmailException;

/**
 * When the create request carries an email, requires it to be a syntactically valid address and
 * stores it lower-cased, mutating the published {@link OwnerFieldsDto} in place so {@link BuildOwner}
 * maps the normalised value. A missing email is left untouched; an invalid one is rejected with 400.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final java.util.Set<String> DISPOSABLE_DOMAINS =
            java.util.Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidOwnerEmailException(email);
        }
        String normalised = email.toLowerCase();
        if (DISPOSABLE_DOMAINS.contains(normalised.substring(normalised.lastIndexOf('@') + 1))) {
            throw new InvalidOwnerEmailException(email);
        }
        request.setEmail(normalised);
    }
}
