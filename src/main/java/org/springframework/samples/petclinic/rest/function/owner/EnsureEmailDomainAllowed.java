package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner whose email address uses a disposable domain, but only WHEN AN EMAIL IS
 * PRESENT — a request with no email passes through unchanged (email is optional). The domain
 * is the part after the last {@code '@'}, compared case-insensitively against the
 * disposable-domain blocklist (mailinator.com, tempmail.com, guerrillamail.com). A match is
 * rejected 400 via {@link DisposableEmailDomainException}.
 */
public class EnsureEmailDomainAllowed {

    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isEmpty()) {
            return; // optional: validated only when present
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return; // not a well-formed address; leave shape validation to @Email
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email, domain);
        }
    }
}
