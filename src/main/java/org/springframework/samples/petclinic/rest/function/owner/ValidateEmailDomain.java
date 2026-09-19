package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner request whose email domain is on the disposable-domain blocklist. Email
 * is optional: a null or blank value is left untouched. Runs after {@link NormalizeOwnerEmail}
 * has validated the syntax and lower-cased the address, so the domain is compared in canonical
 * form. A blocked domain is rejected with {@link DisposableEmailDomainException} (400).
 */
public class ValidateEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }
}
