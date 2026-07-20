package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.web.bind.annotation.PathVariable;

public class LoadOwner {

    public void service(@PathVariable(name = "ownerId") Integer ownerId,
            OwnerRepository ownerRepository, Out<Owner> loaded) throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId), "Owner not found: " + ownerId));
    }
}
