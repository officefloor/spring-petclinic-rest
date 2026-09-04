package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner: the record is retained and flagged {@code deleted = true} rather than
 * removed, so a later {@code GET /api/owners/{id}} still returns the owner (with {@code deleted}
 * true). The create endpoint's duplicate/identity checks ignore owners flagged deleted.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
