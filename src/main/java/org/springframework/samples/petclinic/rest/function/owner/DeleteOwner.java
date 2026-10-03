package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes the owner: it flags the record {@code deleted} and saves it, retaining the
 * row rather than removing it. The owner is still readable via {@code GET}, now reporting
 * {@code deleted} true, and the create endpoint's household and soft-match rules
 * ({@link Households#members}, {@link FlagPossibleDuplicate}) ignore it thereafter.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
