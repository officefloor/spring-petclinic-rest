package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single definition of the owner risk flag: raised when an owner warrants a closer look
 * because any one of three softer signals holds — it is a possible duplicate (see
 * {@link FlagPossibleDuplicate}), its email domain is disposable-adjacent (see
 * {@link DisposableEmailDomains#isAdjacent}), or its city is over its soft capacity (see
 * {@link CapacityWarning}). Surfaced on every owner response so callers can spot an owner that
 * cleared the hard create-time rules but still looks suspicious.
 */
final class RiskFlag {

    private RiskFlag() {
    }

    /** Whether {@code owner} trips any of the risk signals against the current owners. */
    static boolean isRaised(Owner owner, OwnerRepository ownerRepository) {
        return owner.getPossibleDuplicateOf() != null
                || DisposableEmailDomains.isAdjacent(owner.getEmail())
                || CapacityWarning.isRaised(owner.getCity(), ownerRepository);
    }
}
