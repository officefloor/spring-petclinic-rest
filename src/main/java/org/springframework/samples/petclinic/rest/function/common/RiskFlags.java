package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's read-only {@code riskFlag}: true when any of the risk signals hold, otherwise
 * false. The signals are the owner being a possible duplicate ({@code possibleDuplicate}), the email
 * domain being disposable-adjacent (see {@link DisposableDomains#isAdjacent(String)}), or the city
 * being over its soft capacity ({@code capacityWarning}). The possible-duplicate and capacity signals
 * are stamped at create time; the disposable-adjacent signal is derived from the stored email.
 */
public final class RiskFlags {

    private RiskFlags() {
    }

    /** True when {@code owner} is a possible duplicate, has a disposable-adjacent email domain, or
     *  is over its city's soft capacity. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || DisposableDomains.isAdjacent(DisposableDomains.domainOf(owner.getEmail()));
    }
}
