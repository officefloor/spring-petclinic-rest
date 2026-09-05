package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create-owner request whose {@code email} domain is on the disposable-domain blocklist.
 * Email is optional, so an absent or blank value is left untouched. Runs after {@link NormalizeEmail}
 * has validated the syntax and lower-cased the value, so the domain is matched case-insensitively.
 * A blocked domain is rejected via {@link DisposableEmailDomainException}, which the global handler
 * turns into a 400.
 */
public class RejectDisposableEmailDomain {

    /** Domains whose addresses are rejected. */
    static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String raw = request.getEmail();
        if (raw == null || raw.isBlank()) {
            return;
        }
        int at = raw.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = raw.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(domain);
        }
    }
}
