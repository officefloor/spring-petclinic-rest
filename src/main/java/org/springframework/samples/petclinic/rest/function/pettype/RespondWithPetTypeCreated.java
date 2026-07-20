package org.springframework.samples.petclinic.rest.function.pettype;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

public class RespondWithPetTypeCreated {

    public void service(@Val PetType petType, PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<PetTypeDto>> response) {
        PetTypeDto dto = petTypeMapper.toPetTypeDto(petType);
        response.send(ResponseEntity.created(URI.create("/api/pettypes/" + petType.getId())).body(dto));
    }
}
