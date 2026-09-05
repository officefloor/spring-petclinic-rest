package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

public class ApplyPetDto {

    public void service(@Val Pet pet, @Val PetDto request, PetMapper petMapper) {
        ApplyPet.apply(pet, request.getName(), request.getBirthDate(), request.getType(), petMapper);
    }
}
