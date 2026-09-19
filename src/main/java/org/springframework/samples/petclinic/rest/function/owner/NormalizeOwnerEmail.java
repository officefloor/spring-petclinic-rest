package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes an optional email on an owner request. Email is not required: a null or blank
 * value is left untouched. When present it must be a syntactically valid address, otherwise
 * the request is rejected with {@link InvalidEmailException} (400). A valid address is stored
 * back on the shared {@code @Val} request lower-cased, so {@link BuildOwner}/{@link ApplyOwner}
 * and the response carry the canonical form.
 */
public class NormalizeOwnerEmail {

    /** Local part and domain each a run of non-space, non-'@' characters, domain dotted. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String raw = request.getEmail();
        if (raw == null || raw.isBlank()) {
            return;
        }
        String trimmed = raw.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(raw);
        }
        request.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }
}
