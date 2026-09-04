package org.springframework.samples.petclinic.model;

import java.util.Set;

/**
 * Read-time risk assessment for an owner. The flag is raised when any single signal fires:
 * the owner is a possible duplicate, its email domain is disposable-adjacent (a known
 * disposable provider or a subdomain of one), or its city is over its soft capacity.
 */
public final class RiskFlag {

    private static final Set<String> DISPOSABLE = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private RiskFlag() {
    }

    /** True when any single risk signal holds for the given owner; otherwise false. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || disposableAdjacent(owner.getEmail());
    }

    /** True when the email's domain is, or is a subdomain of, a known disposable provider. */
    private static boolean disposableAdjacent(String email) {
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return DISPOSABLE.stream().anyMatch(d -> domain.equals(d) || domain.endsWith("." + d));
    }
}
