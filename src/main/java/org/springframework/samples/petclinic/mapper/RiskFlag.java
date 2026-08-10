package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: whether the owner warrants a manual risk review.
 *
 * <p>The flag is {@code true} when any of these hold, else {@code false}:
 * <ul>
 *   <li>the owner is a possible (soft) duplicate — {@link Owner#getPossibleDuplicate()} is true;</li>
 *   <li>its stored email domain is {@link DisposableAdjacent disposable-adjacent};</li>
 *   <li>its city is over its soft capacity — {@link Owner#getCapacityWarning()} is true (the
 *       warning band begins at the soft cap and, since the hard cap of 50 rejects a create, a
 *       stored owner over the soft cap always carries the warning).</li>
 * </ul>
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code riskFlag} expression)
 * rather than a mapper {@code default} method to avoid MapStruct picking it up as an automatic
 * conversion.
 */
final class RiskFlag {

    private RiskFlag() {
    }

    /** True when the owner is a possible duplicate, disposable-adjacent, or over soft capacity. */
    static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || DisposableAdjacent.matches(owner.getEmail())
                || Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
