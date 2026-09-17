package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Create-owner step: rejects the request with a 400 when the email's domain is on the
 * disposable-domain blocklist (see {@link DisposableEmailDomains}). Email is optional, so a
 * missing or blank value is left untouched. Runs after {@link NormalizeOwnerEmail} (so the
 * address is already validated and lower-cased) and before {@link NormalizeOwnerAddress}.
 */
public class EnsureEmailNotDisposable {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (DisposableEmailDomains.isBlocked(email)) {
            throw new DisposableEmailDomainException();
        }
    }
}
