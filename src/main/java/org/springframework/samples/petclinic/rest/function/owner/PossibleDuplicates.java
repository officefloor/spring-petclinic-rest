package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft duplicate matching: a new owner is a <em>possible</em> duplicate of an existing owner
 * when they belong to the same {@link Households household} (same last name and postcode) yet
 * were given a different telephone number.
 *
 * <p>Unlike a household duplicate (see {@link EnsureUniqueHousehold}), which is rejected
 * outright, a possible duplicate that reaches this point has declared {@code sharesHousehold};
 * it is still created and merely flagged with the id of the owner it resembles. The differing
 * telephone is what distinguishes it from an exact re-registration.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * The id of the earliest existing owner the given owner possibly duplicates — same
     * household, different telephone — or empty when there is no such owner.
     */
    static Optional<Integer> matchFor(Owner owner, OwnerRepository ownerRepository) {
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (matches(owner, existing) && (match == null || existing.getId() < match.getId())) {
                match = existing;
            }
        }
        return Optional.ofNullable(match).map(Owner::getId);
    }

    private static boolean matches(Owner owner, Owner existing) {
        return Households.sameHousehold(owner, existing)
                && !sameTelephone(owner.getTelephone(), existing.getTelephone());
    }

    private static boolean sameTelephone(String a, String b) {
        return a != null && a.equals(b);
    }
}
