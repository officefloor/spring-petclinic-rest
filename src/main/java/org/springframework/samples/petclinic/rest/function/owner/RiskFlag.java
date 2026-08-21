package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Read-time risk signal for an owner. The response's {@code riskFlag} is true when any of three
 * signals hold, otherwise false:
 *
 * <ul>
 * <li>the owner is a possible duplicate - the stored {@link Owner#getPossibleDuplicate()} soft-match
 * flag set by {@link AssignPossibleDuplicate};</li>
 * <li>the email domain is disposable-adjacent - {@link OwnerEmail#isDisposableAdjacent(String)};</li>
 * <li>the city is over its soft capacity - the same 40-owner band that raises
 * {@link CapacityWarning}, short of the hard limit of 50.</li>
 * </ul>
 *
 * <p>Purely derived at read time from already-persisted state, so it carries no storage of its own.
 */
final class RiskFlag {

    private RiskFlag() {
    }

    static boolean of(Owner owner, OwnerRepository ownerRepository) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || OwnerEmail.isDisposableAdjacent(owner.getEmail())
                || CapacityWarning.warning(owner, ownerRepository);
    }
}
