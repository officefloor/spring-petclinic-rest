package org.springframework.samples.petclinic.mapper;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's 'riskFlag'. It is true when any risk signal holds: the owner is a
 * possible duplicate, the email domain is disposable-adjacent (shares a base label with a
 * known disposable-mail provider), or the city is over its soft capacity (the capacity
 * warning threshold); otherwise false.
 */
final class RiskFlags {

    /** Base labels of known disposable-mail providers; a domain sharing one is disposable-adjacent. */
    private static final Set<String> DISPOSABLE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlags() {
    }

    /** Whether {@code owner} trips any risk signal. */
    static boolean of(Owner owner) {
        return owner.getPossibleDuplicate()
            || owner.getCapacityWarning()
            || disposableAdjacent(owner.getEmail());
    }

    private static boolean disposableAdjacent(String email) {
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
