package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.owner.EmailNormalizer;

/**
 * The owner's single risk follow-up signal, an OR of the individual risk markers already derived for an
 * owner: a possible duplicate ({@link Owner#getPossibleDuplicate()}), a city that was over its soft
 * capacity at creation ({@link Owner#getCapacityWarning()}), or an email whose domain is
 * disposable-adjacent (see {@link DisposableEmailDomains#isDisposableAdjacent(String)}). Composed at
 * response time from the owner's stored state, so it is derived rather than stored on the entity.
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    /** Whether {@code owner} carries any risk marker; false for a {@code null} owner. */
    public static boolean of(Owner owner) {
        if (owner == null) {
            return false;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || DisposableEmailDomains.isDisposableAdjacent(EmailNormalizer.domainOf(owner.getEmail()));
    }
}
