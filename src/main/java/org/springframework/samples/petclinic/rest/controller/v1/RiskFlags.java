package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the composite {@code riskFlag}: an owner is risky when any single
 * signal fires — a possible (soft) duplicate, a disposable-adjacent email
 * domain, or a city already over its soft capacity.
 */
final class RiskFlags {

    private RiskFlags() {
    }

    /** True when the owner trips any one of the three risk signals. */
    static boolean isRisky(Collection<Owner> owners, Owner owner) {
        return PossibleDuplicates.matchId(owners, owner) != null
            || DisposableDomains.isBlocked(owner)
            || CityCapacity.approaching(owners, owner);
    }
}
