package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;

/**
 * On create, rejects the owner when its email domain is on the disposable-domain blocklist.
 * An absent or domain-less email is left untouched.
 */
public class EnsureOwnerEmailNotDisposable {

    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    public void service(@Val Owner owner) throws DisposableEmailException {
        String email = owner.getEmail();
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase();
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailException("Email domain is not allowed");
        }
    }
}
