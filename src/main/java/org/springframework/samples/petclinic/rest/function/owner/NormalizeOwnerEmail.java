package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Create-owner step: email is optional, so a missing or blank value is left untouched.
 * When present it must be a syntactically valid address, otherwise the request is rejected
 * with a 400; a valid address is stored lower-cased so it is persisted and returned in
 * canonical form. Runs after {@link NormalizeOwnerTelephone} and before {@link BuildOwner},
 * mutating the published body in place. Mirrors {@link NormalizeOwnerTelephone}.
 */
public class NormalizeOwnerEmail {

    /** A local part, an '@', then a dotted domain with a non-numeric top-level label. */
    private static final Pattern EMAIL =
        Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)*\\.[^@\\s.\\d]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new InvalidEmailException();
        }
        request.setEmail(normalized);
    }
}
