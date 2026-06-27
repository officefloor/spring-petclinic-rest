package org.springframework.samples.petclinic.rest.endpoint;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Validated
public class PetEndpoint {

    public void getPet(
            @PathVariable(name = "petId") Integer petId,
            PetRepository petRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<PetDto>> response) {
        Pet pet = findById(() -> petRepository.findById(petId));
        if (pet == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(petMapper.toPetDto(pet)));
    }

    public void listPets(
            PetRepository petRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<List<PetDto>>> response) {
        Collection<Pet> pets = petRepository.findAll();
        if (pets.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(new ArrayList<>(petMapper.toPetsDto(pets))));
    }

    public void updatePet(
            @PathVariable(name = "petId") Integer petId,
            @Valid @RequestBody PetDto petDto,
            PetRepository petRepository,
            PetTypeRepository petTypeRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<PetDto>> response) {
        Pet currentPet = findById(() -> petRepository.findById(petId));
        if (currentPet == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        currentPet.setBirthDate(petDto.getBirthDate());
        currentPet.setName(petDto.getName());
        PetType petType = findById(() -> petTypeRepository.findById(petDto.getType().getId()));
        currentPet.setType(petType);
        petRepository.save(currentPet);
        response.send(ResponseEntity.status(204).body(petMapper.toPetDto(currentPet)));
    }

    public void deletePet(
            @PathVariable(name = "petId") Integer petId,
            PetRepository petRepository,
            ObjectResponse<ResponseEntity<PetDto>> response) {
        Pet pet = findById(() -> petRepository.findById(petId));
        if (pet == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        petRepository.delete(pet);
        response.send(ResponseEntity.noContent().build());
    }

    private static <T> T findById(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            return null;
        }
    }
}
