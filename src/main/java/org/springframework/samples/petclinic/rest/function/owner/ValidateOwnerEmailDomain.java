package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner email whose domain is on the disposable-domain blocklist with a 400.
 * Runs after {@code NormalizeOwnerEmail}, so a present email is already syntactically
 * valid and lower-cased. An absent or blank email is left untouched (email is optional).
 */
public class ValidateOwnerEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return; // syntax already enforced upstream; nothing to check here
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
