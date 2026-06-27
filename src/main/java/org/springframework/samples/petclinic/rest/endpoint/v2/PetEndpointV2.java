package org.springframework.samples.petclinic.rest.endpoint.v2;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.dto.PetPageDto;
import org.springframework.web.bind.annotation.RequestParam;

public class PetEndpointV2 {

    public void listPetsPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            PetRepository petRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<PetPageDto>> response) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id"));
        Page<Pet> pets = petRepository.findAll(pageRequest);
        response.send(ResponseEntity.ok(petMapper.toPetPageDto(pets)));
    }
}
