package org.springframework.samples.petclinic.service;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: derive the owner's {@code riskFlag} for the response. It is true when any one of
 * three signals holds — the owner is a possible duplicate, its email domain is disposable-adjacent
 * (shares a known disposable provider's base label, even on a different TLD or as a subdomain), or
 * its city is over its soft capacity (the {@link OwnerCapacityWarningPolicy} band) — and false
 * otherwise. Kept as a small, self-contained unit so the flag can be derived during owner-to-DTO
 * mapping without adding complexity to the mapper, controller or service.
 */
public final class OwnerRiskFlagPolicy {

    private static final Set<String> DISPOSABLE_LABELS = Set.of(
        "mailinator", "tempmail", "guerrillamail");

    private OwnerRiskFlagPolicy() {
    }

    /**
     * @param owner the owner being mapped
     * @return true when the owner is a possible duplicate, its email is disposable-adjacent, or its
     *         city is over its soft capacity
     */
    public static boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || disposableAdjacent(owner.getEmail())
            || OwnerCapacityWarningPolicy.capacityWarning(owner);
    }

    /** True when the email's domain carries a known disposable provider's base label. */
    private static boolean disposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        for (String label : DISPOSABLE_LABELS) {
            if (domain.equals(label) || domain.endsWith("." + label)
                || domain.startsWith(label + ".") || domain.contains("." + label + ".")) {
                return true;
            }
        }
        return false;
    }
}
