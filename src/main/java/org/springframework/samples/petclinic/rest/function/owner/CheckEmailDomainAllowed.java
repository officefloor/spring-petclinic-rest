package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner {@code email} whose domain is on the disposable-domain blocklist with a
 * {@link DisposableEmailDomainException} (400). Runs after {@link NormalizeOwnerEmail}, so a
 * null/blank email is already absent and a present value is a syntactically valid, lower-cased
 * address — the domain is the part after the final {@code @}.
 */
public class CheckEmailDomainAllowed {

    /** Domains rejected regardless of the local part. */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
