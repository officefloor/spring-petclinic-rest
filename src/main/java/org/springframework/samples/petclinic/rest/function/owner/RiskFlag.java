package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The owner's composite risk flag reported on a single-owner response: whether the owner warrants
 * review. It ORs three independently-owned signals rather than re-deriving them — the stored
 * soft-duplicate flag ({@link DetectPossibleDuplicate}), a disposable-adjacent email domain
 * ({@link Emails#isDisposableAdjacent}), and a city over its soft capacity
 * ({@link Cities#isOverSoftCapacity}).
 */
final class RiskFlag {

    private RiskFlag() {
    }

    /** Whether {@code owner} is a possible duplicate, has a disposable-adjacent email domain, or
     * lives in a city over its soft capacity. */
    static boolean of(Owner owner, OwnerRepository ownerRepository) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || (owner.hasEmail() && Emails.isDisposableAdjacent(owner.getEmail()))
                || Cities.isOverSoftCapacity(ownerRepository, owner.getCity());
    }
}
