package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-delete step of {@code DELETE /api/owners/{ownerId}}. The owner's row is retained; the owner is
 * only flagged {@code deleted = true} and re-saved, so a later {@code GET /api/owners/{ownerId}} still
 * returns it (with {@code deleted} true) and the create endpoint's duplicate/identity checks ignore it.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
