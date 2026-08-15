package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.function.common.DisposableDomains;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has published the
 * request (with the email already normalized to lower case). Rejects the request when the email's
 * domain is on the disposable-domain blocklist, so a throwaway address is a 400 Bad Request rather
 * than a create. Email is optional; an absent or domain-less value is left for the other rules.
 */
public class RejectDisposableEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String domain = DisposableDomains.domainOf(request.getEmail());
        if (DisposableDomains.isBlocked(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
