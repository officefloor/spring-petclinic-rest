package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: a single boolean summarising whether the owner warrants a
 * closer look. It is {@code true} when <em>any</em> of the following hold, otherwise {@code false}:
 *
 * <ul>
 *   <li>the owner is a possible (soft, non-blocking) duplicate — {@code possibleDuplicate};</li>
 *   <li>the owner's email domain is disposable-adjacent (a sub-domain of a throwaway provider) —
 *       see {@link DisposableEmailDomains#isDisposableAdjacent(String)};</li>
 *   <li>the owner's city is over its soft capacity — the per-city capacity warning band, i.e.
 *       {@code capacityWarning}.</li>
 * </ul>
 *
 * <p>Derived purely from the owner's already-computed state, so it is stable across both the
 * create response and later reads.
 */
public final class RiskFlags {

    private RiskFlags() {
    }

    /**
     * @param owner the owner (must not be {@code null}).
     * @return {@code true} when the owner is a possible duplicate, has a disposable-adjacent email
     *         domain, or sits in a city over its soft capacity; {@code false} otherwise.
     */
    public static boolean flagFor(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || DisposableEmailDomains.isDisposableAdjacent(owner.getEmail());
    }
}
