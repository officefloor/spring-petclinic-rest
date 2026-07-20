package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;

public class DeletePet {

    public void service(@Val Pet pet, PetRepository petRepository) {
        petRepository.delete(pet);
    }
}
