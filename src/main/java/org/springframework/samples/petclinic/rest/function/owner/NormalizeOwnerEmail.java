package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalises the optional owner {@code email}. Email is optional, so a null/blank value is left
 * untouched and stored as absent. When present it must be a syntactically valid address; the
 * cleaned, lower-cased value is written back onto the (shared) request DTO so it is stored and
 * returned as {@code email}. Anything present but syntactically invalid is rejected 400 via
 * {@link InvalidEmailException}.
 */
public class NormalizeOwnerEmail {

    /**
     * A pragmatic syntactic check: a non-empty local part, a single {@code @}, and a domain with at
     * least one dot-separated label ending in a letters-only top-level label of length 2+.
     */
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            request.setEmail(null);
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(trimmed.toLowerCase());
    }
}
