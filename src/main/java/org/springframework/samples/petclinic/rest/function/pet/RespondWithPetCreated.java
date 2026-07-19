package org.springframework.samples.petclinic.rest.function.pet;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.dto.PetDto;

public class RespondWithPetCreated {

    public void service(@Val Pet pet, PetMapper petMapper, ObjectResponse<ResponseEntity<PetDto>> response) {
        PetDto dto = petMapper.toPetDto(pet);
        response.send(ResponseEntity.created(URI.create("/api/pets/" + pet.getId())).body(dto));
    }
}
