package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;

public class SavePet {

    public void service(@Val Pet pet, PetRepository petRepository, PetTypeRepository petTypeRepository) {
        pet.setType(petTypeRepository.findById(pet.getType().getId()));
        petRepository.save(pet);
    }
}
