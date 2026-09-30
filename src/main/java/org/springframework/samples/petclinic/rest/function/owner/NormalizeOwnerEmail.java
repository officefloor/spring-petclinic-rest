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
            request.setEmail(normalize(email));
        }
    }

    /**
     * The single source of truth for an email's canonical form: lower-cased. Null-safe so it can
     * canonicalize an existing owner's (possibly absent) stored value. The canonical value feeds the
     * derived {@link org.springframework.samples.petclinic.model.Owner#getIdentityKey() identityKey},
     * so the "same email" definition can never drift from how emails are stored and returned.
     */
    static String normalize(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }
}
