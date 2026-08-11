package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the owner {@code email}: it is optional, so a null or blank value is left
 * absent. When present it must be a syntactically valid address; otherwise
 * {@link InvalidEmailException} (400) is raised. A valid address is stored lower-cased on
 * the body so {@link BuildOwner}/{@link ApplyOwner} map and {@link SaveOwner} stores the
 * canonical form, and {@code GET} returns it lower-cased.
 *
 * <p>Mutates the validated body in place — {@code @Val} yields the same object the earlier
 * step published.
 */
public class NormalizeOwnerEmail {

    /** Requires a non-empty local part, an {@code @}, and a domain containing a dot,
     *  with no whitespace or extra {@code @}. Rejects values like {@code "not-an-email"}. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            request.setEmail(null);
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
