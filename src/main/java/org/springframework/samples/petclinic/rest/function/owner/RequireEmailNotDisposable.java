package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner request whose {@code email} sits on a disposable-domain blocklist (see
 * {@link DisposableEmailDomains}). Runs after {@code NormalizeOwnerEmail}, so a blank email is already
 * skipped and a present address is lower-cased. A blocked domain is reported as a 400 by
 * {@code DisposableEmailDomainExceptionHandler}.
 */
public class RequireEmailNotDisposable {

    public void service(@Val OwnerFieldsDto owner) throws DisposableEmailDomainException {
        String email = owner.getEmail();
        if (EmailNormalizer.isBlank(email)) {
            return;
        }
        if (DisposableEmailDomains.isDisposable(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
