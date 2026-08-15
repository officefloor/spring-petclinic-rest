package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class DeleteOwner {

    /**
     * Soft-delete: flag the owner deleted and keep the row, rather than removing it. The record
     * stays readable via {@code GET /owners/{id}} and is ignored by the create endpoint's
     * duplicate/identity checks (see {@link CheckOwnerIdentityUnique} and
     * {@link AssignPossibleDuplicate}).
     */
    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
