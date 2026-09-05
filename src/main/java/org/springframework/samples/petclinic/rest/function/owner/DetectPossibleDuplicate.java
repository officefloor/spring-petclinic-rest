package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-match duplicate detection for create-owner. Runs after the hard-duplicate check
 * ({@link RejectDuplicateOwner}), so the owner being created has a telephone not already in use.
 * If it nonetheless shares an existing owner's lastName (case-insensitively) and postcode while
 * using a different telephone, it is still created but flagged: {@code possibleDuplicate} is set
 * true and {@code possibleDuplicateOf} to the matching owner's id (the lowest id when several
 * match). Otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is null.
 *
 * <p>Runs before the owner is saved, so {@link OwnerRepository#findAll()} sees only owners that
 * existed before this create.
 */
public class DetectPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Owner match = null;
        if (lastName != null && postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (lastName.equalsIgnoreCase(existing.getLastName())
                        && postcode.equals(existing.getPostcode())
                        && (telephone == null || !telephone.equals(existing.getTelephone()))) {
                    if (match == null || existing.getId() < match.getId()) {
                        match = existing;
                    }
                }
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        } else {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
        }
    }
}
