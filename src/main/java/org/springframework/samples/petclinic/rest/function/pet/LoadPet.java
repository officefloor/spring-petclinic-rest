package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;
import org.springframework.web.bind.annotation.PathVariable;

public class LoadPet {

    public void service(@PathVariable(name = "petId") Integer petId,
            PetRepository petRepository, Out<Pet> loaded) throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> petRepository.findById(petId), "Pet not found: " + petId));
    }
}
