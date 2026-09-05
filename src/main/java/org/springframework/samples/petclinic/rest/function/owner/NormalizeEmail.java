package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes an owner request's optional {@code email}. Email is optional, so an absent or blank
 * value is left untouched. When present it must be a syntactically valid address; otherwise the
 * request is rejected via {@link InvalidEmailException}, which the global handler turns into a 400.
 * A valid value is lower-cased and written back onto the request (the same object the later
 * {@link BuildOwner}/{@link ApplyOwner} step maps to the entity), so it is stored and returned
 * lower-cased as {@code email}.
 */
public class NormalizeEmail {

    /** A basic syntactic check: non-empty local part, '@', domain with at least one dot. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String raw = request.getEmail();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String email = raw.trim();
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(raw);
        }
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
