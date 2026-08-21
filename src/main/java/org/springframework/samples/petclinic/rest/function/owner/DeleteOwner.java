package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class DeleteOwner {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Soft delete: flag the owner deleted and retain the row rather than removing it, so
        // GET /api/owners/{id} still returns it (with 'deleted' true) and the create endpoint's
        // duplicate/identity checks can ignore it.
        owner.setDeleted(true);
        ownerRepository.save(owner);
    }
}
