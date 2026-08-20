package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner whose email uses a disposable-domain provider. Runs after
 * {@link NormalizeOwnerEmail}, so the address has already been validated and lower-cased. Email is
 * optional: when absent (null or blank) the owner is left unchanged. When the domain is on the
 * blocklist a {@link DisposableEmailDomainException} is thrown for a 400.
 */
public class RejectDisposableEmailDomain {

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
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
