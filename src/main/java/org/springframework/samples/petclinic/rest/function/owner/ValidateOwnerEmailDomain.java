package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner email whose domain is on the {@link DisposableEmailDomains disposable-domain
 * blocklist} with a 400. Runs after {@code NormalizeOwnerEmail}, so a present email is already
 * syntactically valid and lower-cased. An absent or blank email is left untouched (email is optional).
 */
public class ValidateOwnerEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (DisposableEmailDomains.isBlocked(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
