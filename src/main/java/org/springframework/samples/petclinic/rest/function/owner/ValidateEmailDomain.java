package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.util.DisposableEmailDomains;

/**
 * Rejects an owner request whose email domain is on the disposable-domain blocklist. Email
 * is optional: a null or blank value is left untouched. Runs after {@link NormalizeOwnerEmail}
 * has validated the syntax and lower-cased the address, so the domain is compared in canonical
 * form. A blocked domain is rejected with {@link DisposableEmailDomainException} (400). Softer,
 * disposable-adjacent domains are not rejected here; they are surfaced as a read-time risk flag.
 */
public class ValidateEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String domain = DisposableEmailDomains.domainOf(request.getEmail());
        if (DisposableEmailDomains.isBlocked(domain)) {
            throw new DisposableEmailDomainException(request.getEmail());
        }
    }
}
