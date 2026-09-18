package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.util.StringUtils;

/**
 * Normalizes the optional email of an owner request in place: when present it must be a
 * syntactically valid address, and is stored lower-cased. An absent (or blank) email is
 * left untouched, since email is optional. Rejects a present-but-invalid email with 400.
 */
public class NormalizeOwnerEmail {

    /** local-part '@' domain, no spaces, and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (!StringUtils.hasText(email)) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(email.toLowerCase());
    }
}
