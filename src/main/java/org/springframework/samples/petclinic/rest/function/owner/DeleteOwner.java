package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Soft delete: retain the record, flag it deleted. The row survives so GET still
        // returns the owner (with deleted true) and the create endpoint can ignore it.
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
