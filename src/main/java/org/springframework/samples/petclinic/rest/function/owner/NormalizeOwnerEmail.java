package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Email is optional. When absent (null or blank) this step leaves the request untouched. When
 * present it must be a syntactically valid address; the value is lower-cased in place (so the
 * mapper stores and later steps return the normalized form) and an invalid address is rejected
 * with 400 via {@link InvalidEmailException}.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String raw = request.getEmail();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String email = raw.toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(raw);
        }
        request.setEmail(email);
    }
}
