package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;

/**
 * Rejects an owner request whose email domain is on the disposable-domain blocklist with
 * 400. Runs after {@link NormalizeOwnerEmail} has validated the syntax and lower-cased the
 * address, so the domain is simply the text after the '@'. An absent email is allowed. A
 * disposable-<em>adjacent</em> domain (a subdomain of a blocked one) is not rejected here but
 * later raises the owner's risk flag (see {@link DisposableDomains} and {@link ResolveRiskFlag}).
 */
public class RejectDisposableEmail {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailException {
        String domain = DisposableDomains.domainOf(request.getEmail());
        if (DisposableDomains.isBlocked(domain)) {
            throw new DisposableEmailException(domain);
        }
    }
}
