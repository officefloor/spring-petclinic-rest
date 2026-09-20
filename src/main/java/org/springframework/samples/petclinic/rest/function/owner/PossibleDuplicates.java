package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft duplicate matching: a new owner is a <em>possible</em> duplicate of an existing owner
 * when their last names sound alike (same {@link Soundex#encode Soundex}) and they share a
 * postcode, yet their {@link IdentityKeys identity keys} differ — typically because the
 * telephone (part of the key) differs.
 *
 * <p>Unlike a duplicate identity (see {@link EnsureUniqueIdentity}), which is rejected outright
 * as a re-registration, a possible duplicate is still created and merely flagged with the id of
 * the owner it resembles. The differing identity key is what distinguishes it from an exact
 * re-registration.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * The id of the earliest existing owner the given owner possibly duplicates — matching
     * last-name Soundex and postcode but a different identity key — or empty when there is no
     * such owner.
     */
    static Optional<Integer> matchFor(Owner owner, OwnerRepository ownerRepository) {
        Owner match = null;
        for (Owner existing : ownerRepository.findAllActive()) {
            if (matches(owner, existing) && (match == null || existing.getId() < match.getId())) {
                match = existing;
            }
        }
        return Optional.ofNullable(match).map(Owner::getId);
    }

    private static boolean matches(Owner owner, Owner existing) {
        return Soundex.encode(owner.getLastName()).equals(Soundex.encode(existing.getLastName()))
                && Objects.equals(owner.getPostcode(), existing.getPostcode())
                && !IdentityKeys.of(owner).equals(IdentityKeys.of(existing));
    }
}
