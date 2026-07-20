package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Ownership-scoped lookup: a pet that exists but belongs to a different owner is not found here.
 */
public class LoadOwnersPet {

    public void service(@Val Owner owner, @PathVariable(name = "petId") Integer petId,
            Out<Pet> loaded) throws NotFoundException {
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            throw new NotFoundException("Pet not found for owner " + owner.getId() + ": " + petId);
        }
        loaded.set(pet);
    }
}
