package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: rather than removing the row, it flags the owner {@code deleted} and saves
 * it, so the record is retained and still readable via {@code GET /api/owners/{id}}. The create
 * endpoint's duplicate/identity checks ({@link EnsureUniqueIdentity}, {@link FlagPossibleDuplicate})
 * then ignore owners flagged this way.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
