package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

public class RespondWithPetType {

    public void service(@Val PetType petType, PetTypeMapper petTypeMapper, ObjectResponse<PetTypeDto> response) {
        response.send(petTypeMapper.toPetTypeDto(petType));
    }
}
