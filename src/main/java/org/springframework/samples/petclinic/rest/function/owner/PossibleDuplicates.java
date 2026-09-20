package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Optional;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft duplicate matching: a new owner is a <em>possible</em> duplicate of an existing owner
 * when they share the same last name (compared case-insensitively, ignoring surrounding and
 * internal whitespace) and the same postcode, yet were given a different telephone number.
 *
 * <p>Unlike a hard identity duplicate (see {@link IdentityKeys}), which is rejected outright,
 * a possible duplicate is still created and merely flagged with the id of the owner it
 * resembles. The differing telephone is what keeps it from being a hard duplicate in the first
 * place.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /**
     * The id of the earliest existing owner the given owner possibly duplicates — same last
     * name and postcode, different telephone — or empty when there is no such owner.
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
        return samePostcode(owner.getPostcode(), existing.getPostcode())
                && sameLastName(owner.getLastName(), existing.getLastName())
                && !sameTelephone(owner.getTelephone(), existing.getTelephone());
    }

    private static boolean samePostcode(String a, String b) {
        return a != null && b != null && a.trim().equals(b.trim());
    }

    private static boolean sameLastName(String a, String b) {
        return normalizeName(a).equals(normalizeName(b)) && !normalizeName(a).isEmpty();
    }

    private static boolean sameTelephone(String a, String b) {
        return a != null && a.equals(b);
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
