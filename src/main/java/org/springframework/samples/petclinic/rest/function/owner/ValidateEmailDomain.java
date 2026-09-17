package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.util.DisposableEmailDomains;

/**
 * Rejects a create request whose email is registered under a disposable-mail provider (see
 * {@link DisposableEmailDomains}). Runs after {@link NormalizeEmail}, so it judges the trimmed,
 * lower-cased form and can trust the address is syntactically valid. A missing or blank email is
 * allowed, keeping email optional. A blocked domain is a 400 via {@link DisposableEmailDomainException}.
 */
public class ValidateEmailDomain {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String domain = EmailNormalizer.domainOf(email);
        if (DisposableEmailDomains.isBlocked(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
