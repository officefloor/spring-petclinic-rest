package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the owner's optional email so it is stored and returned lower-cased, mutating
 * the validated request in place so later steps persist and return the normalized value.
 * Syntactic validity is enforced declaratively by the {@code @Email} constraint on the
 * request DTO (checked at {@link ValidateOwner}), so a present but invalid address is already
 * rejected 400 before this step runs; a missing (null) email is left untouched.
 */
public class NormalizeOwnerEmail {

    public void service(@Val OwnerFieldsDto request) {
        String email = request.getEmail();
        if (email != null) {
            request.setEmail(email.toLowerCase(Locale.ROOT));
        }
    }
}
