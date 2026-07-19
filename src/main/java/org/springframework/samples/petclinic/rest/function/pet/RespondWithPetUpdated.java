package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

public class RespondWithPetUpdated {

    public void service(@Val Pet pet, PetMapper petMapper, ObjectResponse<ResponseEntity<PetDto>> response) {
        response.send(ResponseEntity.status(HttpStatus.NO_CONTENT).body(petMapper.toPetDto(pet)));
    }
}
