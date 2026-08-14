package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner request whose {@code email} domain is on the disposable-domain blocklist
 * (mailinator.com, tempmail.com, guerrillamail.com) with a 400 via
 * {@link DisposableEmailDomainException}. Email is optional: a null or blank value is left
 * untouched. Runs after {@link NormalizeOwnerEmail}, which validates syntax and lower-cases the
 * address, so the domain compares case-insensitively against the lower-case blocklist.
 */
public class RejectDisposableEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return; // email is optional
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return; // syntax already validated upstream; nothing to block
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
