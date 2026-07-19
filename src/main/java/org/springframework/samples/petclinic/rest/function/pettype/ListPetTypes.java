package org.springframework.samples.petclinic.rest.function.pettype;

import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;

public class ListPetTypes {

    public void service(PetTypeRepository petTypeRepository, PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<List<PetTypeDto>>> response) {
        List<PetTypeDto> petTypes = petTypeMapper.toPetTypeDtos(petTypeRepository.findAll());
        if (petTypes.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(petTypes));
    }
}
