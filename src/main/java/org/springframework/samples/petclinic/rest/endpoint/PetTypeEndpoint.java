package org.springframework.samples.petclinic.rest.endpoint;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Validated
public class PetTypeEndpoint {

    public void listPetTypes(
            PetTypeRepository petTypeRepository,
            PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<List<PetTypeDto>>> response) {
        Collection<PetType> petTypes = petTypeRepository.findAll();
        if (petTypes.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(new ArrayList<>(petTypeMapper.toPetTypeDtos(new ArrayList<>(petTypes)))));
    }

    public void getPetType(
            @PathVariable(name = "petTypeId") Integer petTypeId,
            PetTypeRepository petTypeRepository,
            PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<PetTypeDto>> response) {
        PetType petType = findById(() -> petTypeRepository.findById(petTypeId));
        if (petType == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(petTypeMapper.toPetTypeDto(petType)));
    }

    public void addPetType(
            @Valid @RequestBody PetTypeFieldsDto petTypeFieldsDto,
            PetTypeRepository petTypeRepository,
            PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<PetTypeDto>> response) {
        PetType type = petTypeMapper.toPetType(petTypeFieldsDto);
        petTypeRepository.save(type);
        response.send(ResponseEntity.status(201).body(petTypeMapper.toPetTypeDto(type)));
    }

    public void updatePetType(
            @PathVariable(name = "petTypeId") Integer petTypeId,
            @Valid @RequestBody PetTypeDto petTypeDto,
            PetTypeRepository petTypeRepository,
            PetTypeMapper petTypeMapper,
            ObjectResponse<ResponseEntity<PetTypeDto>> response) {
        PetType currentPetType = findById(() -> petTypeRepository.findById(petTypeId));
        if (currentPetType == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        currentPetType.setName(petTypeDto.getName());
        petTypeRepository.save(currentPetType);
        response.send(ResponseEntity.status(204).body(petTypeMapper.toPetTypeDto(currentPetType)));
    }

    public void deletePetType(
            @PathVariable(name = "petTypeId") Integer petTypeId,
            PetTypeRepository petTypeRepository,
            ObjectResponse<ResponseEntity<PetTypeDto>> response) {
        PetType petType = findById(() -> petTypeRepository.findById(petTypeId));
        if (petType == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        petTypeRepository.delete(petType);
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
