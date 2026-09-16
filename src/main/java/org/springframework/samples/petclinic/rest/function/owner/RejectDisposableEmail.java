package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist with a 400
 * via {@link DisposableEmailException}. Email is optional, so a null or blank value is
 * left untouched. Runs after {@link NormalizeOwnerEmail} so the address has already been
 * validated and lower-cased.
 */
public class RejectDisposableEmail {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (DisposableEmailDomains.isDisposable(email)) {
            throw new DisposableEmailException(email);
        }
    }
}
