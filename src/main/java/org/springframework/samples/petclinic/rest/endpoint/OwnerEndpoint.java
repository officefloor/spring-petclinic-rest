package org.springframework.samples.petclinic.rest.endpoint;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Validated
public class OwnerEndpoint {

    public void listOwners(
            @RequestParam(name = "lastName", required = false) String lastName,
            OwnerRepository ownerRepository,
            OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<List<OwnerDto>>> response) {
        Collection<Owner> owners;
        if (lastName != null) {
            owners = ownerRepository.findByLastName(lastName);
        } else {
            owners = ownerRepository.findAll();
        }
        if (owners.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(new ArrayList<>(ownerMapper.toOwnerDtoCollection(owners))));
    }

    public void getOwner(
            @PathVariable(name = "ownerId") Integer ownerId,
            OwnerRepository ownerRepository,
            OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = findById(() -> ownerRepository.findById(ownerId));
        if (owner == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(ownerMapper.toOwnerDto(owner)));
    }

    public void addOwner(
            @Valid @RequestBody OwnerFieldsDto ownerFieldsDto,
            OwnerRepository ownerRepository,
            OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner owner = ownerMapper.toOwner(ownerFieldsDto);
        ownerRepository.save(owner);
        response.send(ResponseEntity.status(201).body(ownerMapper.toOwnerDto(owner)));
    }

    public void updateOwner(
            @PathVariable(name = "ownerId") Integer ownerId,
            @Valid @RequestBody OwnerFieldsDto ownerFieldsDto,
            OwnerRepository ownerRepository,
            OwnerMapper ownerMapper,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        Owner currentOwner = findById(() -> ownerRepository.findById(ownerId));
        if (currentOwner == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        currentOwner.setAddress(ownerFieldsDto.getAddress());
        currentOwner.setCity(ownerFieldsDto.getCity());
        currentOwner.setFirstName(ownerFieldsDto.getFirstName());
        currentOwner.setLastName(ownerFieldsDto.getLastName());
        currentOwner.setTelephone(ownerFieldsDto.getTelephone());
        ownerRepository.save(currentOwner);
        response.send(ResponseEntity.status(204).body(ownerMapper.toOwnerDto(currentOwner)));
    }

    public void deleteOwner(
            @PathVariable(name = "ownerId") Integer ownerId,
            OwnerRepository ownerRepository,
            ObjectResponse<ResponseEntity<Void>> response) {
        Owner owner = findById(() -> ownerRepository.findById(ownerId));
        if (owner == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        ownerRepository.delete(owner);
        response.send(ResponseEntity.noContent().build());
    }

    public void addPetToOwner(
            @PathVariable(name = "ownerId") Integer ownerId,
            @Valid @RequestBody PetFieldsDto petFieldsDto,
            OwnerRepository ownerRepository,
            PetRepository petRepository,
            PetTypeRepository petTypeRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<PetDto>> response) {
        Owner owner = findById(() -> ownerRepository.findById(ownerId));
        if (owner == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        Pet pet = petMapper.toPet(petFieldsDto);
        owner.setId(ownerId);
        pet.setOwner(owner);
        pet.getType().setName(null);
        PetType petType = findById(() -> petTypeRepository.findById(pet.getType().getId()));
        pet.setType(petType);
        petRepository.save(pet);
        response.send(ResponseEntity.status(201).body(petMapper.toPetDto(pet)));
    }

    public void updateOwnersPet(
            @PathVariable(name = "ownerId") Integer ownerId,
            @PathVariable(name = "petId") Integer petId,
            @Valid @RequestBody PetFieldsDto petFieldsDto,
            OwnerRepository ownerRepository,
            PetRepository petRepository,
            PetTypeRepository petTypeRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<Void>> response) {
        Owner currentOwner = findById(() -> ownerRepository.findById(ownerId));
        if (currentOwner != null) {
            Pet currentPet = findById(() -> petRepository.findById(petId));
            if (currentPet != null) {
                currentPet.setBirthDate(petFieldsDto.getBirthDate());
                currentPet.setName(petFieldsDto.getName());
                PetType petType = findById(() -> petTypeRepository.findById(petFieldsDto.getType().getId()));
                currentPet.setType(petType);
                petRepository.save(currentPet);
                response.send(ResponseEntity.noContent().build());
                return;
            }
        }
        response.send(ResponseEntity.notFound().build());
    }

    public void addVisitToOwner(
            @PathVariable(name = "ownerId") Integer ownerId,
            @PathVariable(name = "petId") Integer petId,
            @Valid @RequestBody VisitFieldsDto visitFieldsDto,
            VisitRepository visitRepository,
            VisitMapper visitMapper,
            ObjectResponse<ResponseEntity<VisitDto>> response) {
        Visit visit = visitMapper.toVisit(visitFieldsDto);
        Pet pet = new Pet();
        pet.setId(petId);
        visit.setPet(pet);
        visitRepository.save(visit);
        response.send(ResponseEntity.status(201).body(visitMapper.toVisitDto(visit)));
    }

    public void getOwnersPet(
            @PathVariable(name = "ownerId") Integer ownerId,
            @PathVariable(name = "petId") Integer petId,
            OwnerRepository ownerRepository,
            PetMapper petMapper,
            ObjectResponse<ResponseEntity<PetDto>> response) {
        Owner owner = findById(() -> ownerRepository.findById(ownerId));
        if (owner != null) {
            Pet pet = owner.getPet(petId);
            if (pet != null) {
                response.send(ResponseEntity.ok(petMapper.toPetDto(pet)));
                return;
            }
        }
        response.send(ResponseEntity.notFound().build());
    }

    private static <T> T findById(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            return null;
        }
    }
}
