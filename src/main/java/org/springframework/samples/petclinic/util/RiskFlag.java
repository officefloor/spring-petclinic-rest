package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: true when any risk signal holds, otherwise false. The signals
 * are the owner being a possible duplicate ({@code possibleDuplicate}), the email domain being
 * {@link DisposableDomains#isAdjacent disposable-adjacent}, or the city being over its soft capacity
 * ({@code capacityWarning}, i.e. the city already holds 40 or more owners).
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    /** True when the owner is a possible duplicate, has a disposable-adjacent email, or is over soft capacity. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || DisposableDomains.isAdjacent(owner.getEmail())
                || Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
