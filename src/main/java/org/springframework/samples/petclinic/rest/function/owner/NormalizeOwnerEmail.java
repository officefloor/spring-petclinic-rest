package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerEmailException;

/**
 * Normalizes the owner email when present: requires a syntactically valid address and
 * lower-cases it in place so later steps store and return the lower-cased value. An
 * absent or blank email is left untouched (email is optional). Rejects a present but
 * invalid email with a 400.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidOwnerEmailException(email);
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
