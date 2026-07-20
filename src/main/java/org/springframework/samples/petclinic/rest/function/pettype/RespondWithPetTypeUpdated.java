package org.springframework.samples.petclinic.rest.function.pettype;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

public class RespondWithPetTypeUpdated {

    public void service(@Val PetType petType, PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<PetTypeDto>> response) {
        response.send(ResponseEntity.status(HttpStatus.NO_CONTENT).body(petTypeMapper.toPetTypeDto(petType)));
    }
}
