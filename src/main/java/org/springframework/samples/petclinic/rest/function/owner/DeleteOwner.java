package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Soft delete: flag the owner deleted and keep the row so a later GET still returns it,
        // while the create endpoint's duplicate/identity checks ignore it.
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
