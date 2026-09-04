package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

/**
 * When an email is present it must be a syntactically valid address; it is stored back on the
 * request lower-cased so it is persisted and returned as {@code email}. A malformed address is
 * rejected with 400 via {@link MissingFieldsException}. An absent email is left untouched.
 */
public class NormalizeEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@Val OwnerFieldsDto request) throws MissingFieldsException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new MissingFieldsException(List.of("email"));
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
