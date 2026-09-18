package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Comparator;
import java.util.Optional;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate: an owner that is not a hard identity duplicate (already rejected
 * upstream) but shares an existing owner's lastName and postcode with a different telephone.
 * Records the match as {@code possibleDuplicate} true with {@code possibleDuplicateOf} set to the
 * matched owner's id, else {@code possibleDuplicate} false. Runs before the new owner is persisted
 * so it is not compared against itself.
 */
public class AssignOwnerPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Optional<Owner> match = softMatch(owner, ownerRepository);
        owner.setPossibleDuplicate(match.isPresent());
        owner.setPossibleDuplicateOf(match.map(Owner::getId).orElse(null));
    }

    private static Optional<Owner> softMatch(Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return Optional.empty(); // a shared postcode is required to soft-match
        }
        return ownerRepository.findAll().stream()
                .filter(existing -> sharesLastNameAndPostcode(owner, existing))
                .filter(existing -> !owner.getTelephone().equals(existing.getTelephone()))
                .min(Comparator.comparing(Owner::getId));
    }

    private static boolean sharesLastNameAndPostcode(Owner owner, Owner existing) {
        return owner.getLastName().equalsIgnoreCase(existing.getLastName())
                && owner.getPostcode().equals(existing.getPostcode());
    }
}
