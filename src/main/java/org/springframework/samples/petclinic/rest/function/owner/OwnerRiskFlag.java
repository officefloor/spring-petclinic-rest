package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's derived {@code riskFlag} — a soft review signal raised when
 * an owner is worth a second look even though it was accepted at creation.
 *
 * <p>The flag is {@code true} when ANY of the following hold, and {@code false} otherwise:
 * <ul>
 *   <li>the owner is a possible duplicate ({@link Owner#getPossibleDuplicate possibleDuplicate});</li>
 *   <li>the owner's email domain is {@link OwnerEmail#isDisposableAdjacent disposable-adjacent};</li>
 *   <li>the owner's city is over its soft capacity
 *       ({@link Owner#getCapacityWarning capacityWarning}).</li>
 * </ul>
 *
 * <p>Derived at read time by the owner mapper from the already-stored flags and email, so it needs
 * no column of its own and can never drift from its inputs.
 */
public final class OwnerRiskFlag {

    private OwnerRiskFlag() {
    }

    /** The risk flag for the given owner. */
    public static boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || OwnerEmail.isDisposableAdjacent(owner.getEmail());
    }
}
