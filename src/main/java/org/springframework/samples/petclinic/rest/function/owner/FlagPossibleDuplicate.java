package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records a soft-match duplicate: an existing owner that shares this owner's lastName and postcode
 * but has a different telephone (so it is not a hard {@link EnsureUniqueIdentity} conflict). Sets
 * {@code possibleDuplicate} true and {@code possibleDuplicateOf} to that owner's id, otherwise
 * {@code possibleDuplicate} false. Runs before Save, so the flags persist and are read back on GET,
 * and it sees only the owners that existed before this create.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        for (Owner existing : ownerRepository.findAll()) {
            if (softMatches(owner, existing)) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
        owner.setPossibleDuplicate(false);
    }

    private static boolean softMatches(Owner owner, Owner existing) {
        return owner.getPostcode() != null
                && owner.getLastName().equalsIgnoreCase(existing.getLastName())
                && owner.getPostcode().equals(existing.getPostcode())
                && !owner.getTelephone().equals(existing.getTelephone());
    }
}
