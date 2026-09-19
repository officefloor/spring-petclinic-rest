package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes an owner request's optional email: when present it must be a syntactically
 * valid address, otherwise the request is rejected with a 400. A valid address is stored
 * lower-cased. Mutates the shared request in place so downstream steps ({@link BuildOwner}
 * / {@link ApplyOwner}) persist the normalized value. Absent or blank email is left
 * untouched, since the field is optional.
 */
public class NormalizeOwnerEmail {

    /** Syntactic check: a non-empty local part, an '@', and a dotted domain, no whitespace. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
