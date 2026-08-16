package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Soft-delete: retain the row, just flag it. The create endpoint's duplicate/identity
        // checks ignore flagged owners, and GET still returns the (now deleted) record.
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
