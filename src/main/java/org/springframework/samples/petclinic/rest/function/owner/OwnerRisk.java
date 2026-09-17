package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's single risk flag by combining the risk signals already captured on the owner.
 *
 * <p>The flag is raised when any of these hold, otherwise it is false:
 * <ul>
 * <li>the owner is a possible duplicate (see {@link FlagOwnerPossibleDuplicate});</li>
 * <li>the owner's email domain is disposable-adjacent (see
 * {@link OwnerEmails#isDisposableAdjacentDomain});</li>
 * <li>the owner's city is over its soft capacity - the approaching-capacity warning raised at
 * creation (see {@link FlagOwnerCityCapacityWarning}).</li>
 * </ul>
 */
public final class OwnerRisk {

    private OwnerRisk() {
    }

    /** Whether any risk signal is present for {@code owner}. */
    public static boolean flag(Owner owner) {
        return isPossibleDuplicate(owner)
                || OwnerEmails.isDisposableAdjacentDomain(owner.getEmail())
                || isOverSoftCapacity(owner);
    }

    private static boolean isPossibleDuplicate(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    private static boolean isOverSoftCapacity(Owner owner) {
        return Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
