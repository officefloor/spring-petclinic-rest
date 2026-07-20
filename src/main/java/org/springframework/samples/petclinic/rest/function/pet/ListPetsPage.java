package org.springframework.samples.petclinic.rest.function.pet;

import net.officefloor.web.ObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.rest.dto.PetPageDto;
import org.springframework.web.bind.annotation.RequestParam;

public class ListPetsPage {

    public void service(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            PetRepository petRepository, PetMapper petMapper,
            ObjectResponse<PetPageDto> response) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size, Sort.by("id"));
        Page<Pet> pets = petRepository.findAll(pageable);
        response.send(petMapper.toPetPageDto(pets));
    }
}
