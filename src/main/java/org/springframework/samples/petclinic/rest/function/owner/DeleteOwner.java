package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Delete step of {@code DELETE /api/owners/{id}}: a <em>soft</em> delete. Rather than removing the
 * row, it flags the owner {@code deleted} and saves it, so the record is retained and a later
 * {@code GET /api/owners/{id}} still returns it (with {@code deleted} true). A soft-deleted owner is
 * ignored by the create endpoint's duplicate/identity checks.
 */
public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
