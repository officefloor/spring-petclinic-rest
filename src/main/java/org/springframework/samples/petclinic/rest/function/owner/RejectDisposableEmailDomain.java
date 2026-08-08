package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist. Runs after
 * {@link NormalizeOwnerEmail}, so the email is already syntactically valid and lower-cased. When the
 * email is absent the owner is left untouched; when present and its domain is blocked, a 400 is
 * raised via {@link InvalidEmailException}.
 */
public class RejectDisposableEmailDomain {

    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val Owner owner) throws InvalidEmailException {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email domain is not allowed");
        }
    }
}
