package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag} for the response. The flag is true when ANY of these hold:
 * <ul>
 * <li>the owner is a possible duplicate ({@link Owner#getPossibleDuplicate()});</li>
 * <li>the owner's email domain is disposable-adjacent ({@link DisposableEmail#isAdjacent(String)});</li>
 * <li>the owner's city is over its soft capacity — the approaching-capacity warning
 *     ({@link Owner#getCapacityWarning()}), set when the city already held 40 or more owners at
 *     creation time (the soft capacity below the hard limit of 50).</li>
 * </ul>
 * Otherwise false. Purely derived from the stored owner, so a plain GET reflects the same value
 * assigned at creation.
 */
public final class OwnerRiskFlag {

    private OwnerRiskFlag() {
    }

    public static boolean forOwner(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || DisposableEmail.isAdjacent(owner.getEmail())
                || Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
