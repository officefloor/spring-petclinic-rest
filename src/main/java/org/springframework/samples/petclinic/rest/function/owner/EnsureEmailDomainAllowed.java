package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.EmailDomains;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner whose email address uses a disposable domain, but only WHEN AN EMAIL IS
 * PRESENT — a request with no email passes through unchanged (email is optional). The domain
 * is the part after the last {@code '@'}, compared case-insensitively against the
 * disposable-domain blocklist ({@link EmailDomains#DISPOSABLE}: mailinator.com, tempmail.com,
 * guerrillamail.com). A match is rejected 400 via {@link DisposableEmailDomainException}.
 * Near-variants (disposable-adjacent domains) are not rejected here; they surface as a soft
 * risk signal on read instead.
 */
public class EnsureEmailDomainAllowed {

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isEmpty()) {
            return; // optional: validated only when present
        }
        String domain = EmailDomains.domainOf(email);
        if (domain == null) {
            return; // not a well-formed address; leave shape validation to @Email
        }
        if (EmailDomains.DISPOSABLE.contains(domain)) {
            throw new DisposableEmailDomainException(email, domain);
        }
    }
}
