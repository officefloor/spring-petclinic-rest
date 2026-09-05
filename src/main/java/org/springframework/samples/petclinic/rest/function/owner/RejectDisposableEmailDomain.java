package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create-owner request whose {@code email} domain is on the disposable-domain blocklist
 * ({@link DisposableEmailDomains#BLOCKED}). Email is optional, so an absent or blank value is left
 * untouched. Runs after {@link NormalizeEmail} has validated the syntax and lower-cased the value, so
 * the domain is matched case-insensitively. A blocked domain is rejected via
 * {@link DisposableEmailDomainException}, which the global handler turns into a 400. The softer
 * "disposable-adjacent" notion (a non-rejecting risk signal) also lives on this blocklist — see
 * {@link DisposableEmailDomains}.
 */
public class RejectDisposableEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String domain = DisposableEmailDomains.domainOf(request.getEmail());
        if (DisposableEmailDomains.isBlocked(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
