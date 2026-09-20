package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a request whose email domain is a known disposable-email provider (see
 * {@link Emails#isDisposableDomain}). An absent (or blank) email is left untouched. Runs after
 * {@link NormalizeOwnerEmail} has validated syntax and canonicalized the address, so only a
 * present, well-formed value reaches this policy check. A blocked domain is rejected via
 * {@link DisposableEmailDomainException}.
 */
public class RejectDisposableEmail {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (Emails.isDisposableDomain(email)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
