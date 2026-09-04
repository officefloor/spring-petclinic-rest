package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes an optional owner email. When absent (null or blank) the request is left untouched, so
 * email stays optional. When present it must be a syntactically valid address; the validated value is
 * lower-cased and stored back on the {@link OwnerFieldsDto} in place so {@link BuildOwner} and
 * {@link ApplyOwner} persist and return the lower-cased form. An invalid address is rejected with 400
 * via {@link InvalidEmailException}.
 */
public class NormalizeOwnerEmail {

    /**
     * A single {@code @}, with at least one non-space/non-{@code @} character on the local side and a
     * dotted domain on the other — enough to reject syntactically malformed addresses.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public void service(@Val OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(trimmed.toLowerCase());
    }
}
