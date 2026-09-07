package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist
 * (mailinator.com, tempmail.com, guerrillamail.com) with a 400. Email is optional,
 * so an absent address is a no-op. Runs on the built/applied {@link Owner} before it
 * is saved; the earlier {@code Build}/{@code Apply} step has already normalized the
 * address to a trimmed, lower-cased form, but the domain is lower-cased here too so
 * the check is robust regardless of ordering.
 */
public class CheckOwnerEmailDomain {

    /** Email domains that are disposable and therefore not allowed. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val Owner owner) throws DisposableEmailDomainException {
        String email = owner.getEmail();
        if (email == null || email.isBlank()) {
            return; // optional when absent
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return; // no domain to check; syntactic validity is enforced elsewhere
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email, domain);
        }
    }
}
