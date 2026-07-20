package org.springframework.samples.petclinic.rest.function.pet;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.dto.PetDto;

public class ListPets {

    public void service(PetRepository petRepository, PetMapper petMapper,
            ObjectResponse<ResponseEntity<List<PetDto>>> response) {
        List<PetDto> pets = new ArrayList<>(petMapper.toPetsDto(petRepository.findAll()));
        if (pets.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(pets));
    }
}
