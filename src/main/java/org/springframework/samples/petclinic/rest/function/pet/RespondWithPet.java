package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

public class RespondWithPet {

    public void service(@Val Pet pet, PetMapper petMapper, ObjectResponse<PetDto> response) {
        response.send(petMapper.toPetDto(pet));
    }
}
