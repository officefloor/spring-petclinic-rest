package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner email whose domain is on the disposable-domain blocklist. Runs after
 * {@link NormalizeOwnerEmail}, so the email is already trimmed, lower-cased and syntactically valid.
 * Email is optional: when absent (null or blank) this step does nothing. A blocked domain is rejected
 * with 400 via {@link DisposableEmailDomainException}.
 */
public class ValidateOwnerEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
