package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;

public class ApplyPetFields {

    public void service(@Val Pet pet, @Val PetFieldsDto request, PetMapper petMapper) {
        pet.setBirthDate(request.getBirthDate());
        pet.setName(request.getName());
        pet.setType(petMapper.toPetType(request.getType()));
    }
}
