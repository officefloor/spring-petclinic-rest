package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Soft-delete step of {@code DELETE /api/owners/{ownerId}}. Rather than removing the row, it flags the
 * loaded owner {@code deleted} and saves it, so the record is retained and {@code GET /api/owners/{id}}
 * still returns the owner with {@code deleted} true. Deleted owners are then ignored by the
 * {@code POST /api/owners} duplicate/identity checks (see {@link CheckOwnerIdentityUnique}).
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
