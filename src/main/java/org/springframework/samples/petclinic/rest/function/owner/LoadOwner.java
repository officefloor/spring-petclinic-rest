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
        Owner owner = Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId);
        // Resolve the household's member count (this persisted owner is included) so the
        // membership points rule can score a household of 3 or more on read.
        owner.setHouseholdSize(Math.max(1, Household.size(owner.getHouseholdId(), ownerRepository)));
        loaded.set(owner);
    }
}
