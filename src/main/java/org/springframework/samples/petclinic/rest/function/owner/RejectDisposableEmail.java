package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;

/**
 * Rejects a new owner whose email domain is on the disposable-domain blocklist. Runs after Build so the
 * email is already normalized to lower case by {@link Owner#setEmail}, and before Save so the reject is a
 * 400 rather than a persisted owner.
 */
public class RejectDisposableEmail {

    private static final Set<String> BLOCKED = Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val Owner owner) throws DisposableEmailException {
        String email = owner.getEmail();
        if (email == null) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at >= 0 && BLOCKED.contains(email.substring(at + 1))) {
            throw new DisposableEmailException(email);
        }
    }
}
