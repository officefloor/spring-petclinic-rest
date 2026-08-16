package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;

/**
 * Normalizes the optional email of the validated body in place. Email is optional: a null
 * or blank value is left untouched. When present it must be a syntactically valid address,
 * otherwise it is rejected 400 via {@link OwnerEmailInvalidException}. A valid address is
 * lower-cased in place so {@link BuildOwner}/{@link ApplyOwner} store, and later
 * reads/responses return, the lower-cased form.
 */
public class NormalizeOwnerEmail {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void service(@Val OwnerFieldsDto request) throws OwnerEmailInvalidException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new OwnerEmailInvalidException(email);
        }
        request.setEmail(email.toLowerCase());
    }
}
