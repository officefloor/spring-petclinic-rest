package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Rejects owners whose email domain is on a disposable-domain blocklist.
 */
final class DisposableDomains {

    private static final Set<String> BLOCKED = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableDomains() {
    }

    /** Whether the owner's email domain is a blocked disposable domain. */
    static boolean isBlocked(Owner owner) {
        String email = owner.getEmail();
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        return BLOCKED.contains(email.substring(at + 1).toLowerCase());
    }
}
