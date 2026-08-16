package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDisposableException;

/**
 * Rejects the create when the owner's email domain is on the disposable-domain blocklist
 * (mailinator.com, tempmail.com, guerrillamail.com). Email is optional: a null or blank value
 * skips the check. Runs after {@link NormalizeOwnerEmail}, which validates syntax and lower-cases
 * the address, so the domain is compared lower-cased. A blocked domain is rejected 400 via
 * {@link OwnerEmailDisposableException}.
 */
public class RequireNonDisposableOwnerEmail {

    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws OwnerEmailDisposableException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return; // syntax already validated upstream; nothing to block
        }
        String domain = email.substring(at + 1).toLowerCase();
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new OwnerEmailDisposableException(email);
        }
    }
}
