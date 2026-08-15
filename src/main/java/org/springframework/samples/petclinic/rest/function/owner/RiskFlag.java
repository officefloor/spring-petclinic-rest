package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner's response carries the {@code riskFlag}. It is raised when any one of
 * three signals holds:
 *
 * <ul>
 * <li>the owner is a possible duplicate ({@link Owner#getPossibleDuplicate()} true, see
 *     {@link AssignPossibleDuplicate});</li>
 * <li>the owner's email domain is disposable-adjacent (see
 *     {@link DisposableDomains#isAdjacent(String)});</li>
 * <li>the owner's city is over its soft capacity &mdash; it already holds {@value #SOFT_CAPACITY}
 *     or more owners, the point at which {@link CapacityWarning} begins warning of the hard limit of
 *     50 enforced by {@link CheckOwnerCityCapacity}.</li>
 * </ul>
 *
 * Otherwise the flag is false. The city is counted case-insensitively across all owners (this one
 * included), mirroring {@link CheckOwnerCityCapacity} and {@link CapacityWarning}, so the create and
 * read responses agree.
 */
final class RiskFlag {

    /** Soft capacity: the owner count at which a city is considered over capacity. */
    private static final long SOFT_CAPACITY = 40;

    private RiskFlag() {
    }

    /** True when the owner is a possible duplicate, has a disposable-adjacent email, or is in a
     *  city that is over its soft capacity. */
    static boolean riskFor(Owner owner, OwnerRepository ownerRepository) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || DisposableDomains.isAdjacent(owner.getEmail())
                || cityOverSoftCapacity(owner, ownerRepository);
    }

    private static boolean cityOverSoftCapacity(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long inCity = ownerRepository.findAll().stream()
                .filter(existing -> city != null && city.equalsIgnoreCase(existing.getCity()))
                .count();
        return inCity >= SOFT_CAPACITY;
    }
}
