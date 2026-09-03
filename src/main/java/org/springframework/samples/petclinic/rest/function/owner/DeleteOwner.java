package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-deletes an owner for {@code DELETE /api/owners/{id}}. The row is retained; the owner is flagged
 * {@code deleted} true and saved, so {@code GET /api/owners/{id}} still returns it (with {@code deleted}
 * true) and the create endpoint's duplicate/identity checks ignore it.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
