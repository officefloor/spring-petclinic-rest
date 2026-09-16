package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The owner risk policy: whether a created owner warrants a manual review. It composes the
 * three existing risk signals rather than re-deriving them — the owner is a possible
 * duplicate ({@link AssignPossibleDuplicate}), its email domain is disposable-adjacent
 * ({@link DisposableEmailDomains#isDisposableAdjacent}), or its city is at or over its soft
 * capacity ({@link CityCapacity#overSoftCapacity}). The flag is true when any signal holds.
 *
 * <p>City capacity depends on the other owners, so this is computed at response time from
 * the repository rather than stored on the owner (see the owner responders).
 */
final class OwnerRisk {

    private OwnerRisk() {
    }

    static boolean of(Owner owner, OwnerRepository repository) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || DisposableEmailDomains.isDisposableAdjacent(owner.getEmail())
            || CityCapacity.overSoftCapacity(repository, owner.getCity());
    }
}
