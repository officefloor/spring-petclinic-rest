package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: {@code true} when the owner is a possible duplicate,
 * their email domain is disposable-adjacent, or their city is over its soft capacity;
 * {@code false} otherwise.
 *
 * <p>A domain is <em>disposable-adjacent</em> when its registrable label (the label immediately
 * before the top-level domain) matches a known disposable provider ({@code mailinator},
 * {@code tempmail}, {@code guerrillamail}) — this catches sibling TLDs and subdomains that slip
 * past the exact-match block applied at creation.
 */
public final class RiskFlag {

    private static final Set<String> DISPOSABLE_BASES = Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    /** Whether {@code owner} should be flagged as risky. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || disposableAdjacent(owner.getEmail());
    }

    private static boolean disposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String[] labels = email.substring(at + 1).trim().toLowerCase(Locale.ROOT).split("\\.");
        return labels.length >= 2 && DISPOSABLE_BASES.contains(labels[labels.length - 2]);
    }
}
