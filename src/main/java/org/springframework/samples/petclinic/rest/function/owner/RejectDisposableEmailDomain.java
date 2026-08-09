package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Rejects an owner whose email domain is on the disposable-domain blocklist. Runs after
 * {@link NormalizeOwnerEmail}, so the email is already syntactically valid and lower-cased. When the
 * email is absent the owner is left untouched; when present and its domain is blocked, a 400 is
 * raised via {@link InvalidEmailException}. The blocklist is shared with
 * {@link DisposableEmailDomains}, which also recognises the softer disposable-adjacent case used by
 * the {@code riskFlag} derivation.
 */
public class RejectDisposableEmailDomain {

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
        if (DisposableEmailDomains.isBlocked(domain)) {
            throw new InvalidEmailException("Email domain is not allowed");
        }
    }
}
