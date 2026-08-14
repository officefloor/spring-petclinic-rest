package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional {@code email} of an owner request. Email is optional: a null or blank
 * value is left untouched (no email). When present it must be a syntactically valid address,
 * otherwise the request is rejected with a 400 via {@link InvalidEmailException}. A valid address is
 * mutated in place (via {@code @Val}) to its lower-cased form, so the stored and returned
 * {@code email} is lower-case. Runs before {@link BuildOwner}/{@code ApplyOwner}.
 */
public class NormalizeOwnerEmail {

    /** A pragmatic single-address check: local part, '@', domain with at least one dot, no spaces. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(trimmed.toLowerCase());
    }
}
