package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes the {@code riskFlag} response flag: true when any of these hold at the time of
 * the response, otherwise false —
 *
 * <ul>
 * <li>the owner is a possible duplicate ({@link Owner#isPossibleDuplicate()}, set at
 * creation by {@link AssignPossibleDuplicate});</li>
 * <li>the email domain is <em>disposable-adjacent</em> (see
 * {@link OwnerEmail#isDisposableAdjacent(String)});</li>
 * <li>the owner's city is over its soft capacity — the same "approaching the hard cap"
 * threshold surfaced as {@code capacityWarning} (see {@link CapacityWarning#forCity}).</li>
 * </ul>
 */
final class RiskFlag {

    private RiskFlag() {
    }

    static boolean forOwner(Owner owner, OwnerRepository ownerRepository) {
        return owner.isPossibleDuplicate()
            || OwnerEmail.isDisposableAdjacent(owner.getEmail())
            || CapacityWarning.forCity(owner, ownerRepository);
    }
}
