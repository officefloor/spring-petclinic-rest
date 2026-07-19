package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;

public class SavePetType {

    public void service(@Val PetType petType, PetTypeRepository petTypeRepository) {
        petTypeRepository.save(petType);
    }
}
