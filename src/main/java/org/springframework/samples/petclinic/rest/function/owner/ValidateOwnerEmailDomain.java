package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist. Email is optional, so
 * an absent (null/blank) email is skipped; a present but syntactically invalid address is already
 * rejected 400 by the {@code @Email} constraint at {@link ValidateOwner}. Runs after
 * {@link NormalizeOwnerEmail} has lower-cased the address, so the domain comparison is
 * case-insensitive. A blocklisted domain is rejected 400 via {@link DisposableEmailDomainException}.
 */
public class ValidateOwnerEmailDomain {

    /** Disposable email domains that are not accepted for owner registration. */
    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val OwnerFieldsDto request) throws DisposableEmailDomainException {
        String email = request.getEmail();
        if (email == null) {
            return; // optional when absent
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return; // no domain part; syntactic validity is enforced by @Email at ValidateOwner
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(
                "Email domain " + domain + " is not accepted (disposable-domain blocklist)");
        }
    }
}
