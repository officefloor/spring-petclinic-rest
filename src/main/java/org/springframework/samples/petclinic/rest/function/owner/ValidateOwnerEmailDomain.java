package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.util.DisposableEmailDomains;

/**
 * Rejects an owner request whose email domain is on the disposable-domain blocklist with
 * a 400. Runs after {@link NormalizeOwnerEmail} has confirmed the address is syntactically
 * valid and lower-cased it. Absent or blank email is left untouched, since the field is
 * optional.
 */
public class ValidateOwnerEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (DisposableEmailDomains.isBlocked(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
