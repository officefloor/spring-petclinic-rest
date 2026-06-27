package org.springframework.samples.petclinic.rest.endpoint;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.samples.petclinic.rest.dto.VetDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Validated
public class VetEndpoint {

    public void listVets(
            VetRepository vetRepository,
            VetMapper vetMapper,
            ObjectResponse<ResponseEntity<List<VetDto>>> response) {
        Collection<Vet> vets = vetRepository.findAll();
        if (vets.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(new ArrayList<>(vetMapper.toVetDtos(vets))));
    }

    public void getVet(
            @PathVariable(name = "vetId") Integer vetId,
            VetRepository vetRepository,
            VetMapper vetMapper,
            ObjectResponse<ResponseEntity<VetDto>> response) {
        Vet vet = findById(() -> vetRepository.findById(vetId));
        if (vet == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(vetMapper.toVetDto(vet)));
    }

    public void addVet(
            @Valid @RequestBody VetDto vetDto,
            VetRepository vetRepository,
            SpecialtyRepository specialtyRepository,
            VetMapper vetMapper,
            ObjectResponse<ResponseEntity<VetDto>> response) {
        Vet vet = vetMapper.toVet(vetDto);
        if (vet.getNrOfSpecialties() > 0) {
            List<Specialty> vetSpecialties = specialtyRepository.findSpecialtiesByNameIn(
                vet.getSpecialties().stream().map(Specialty::getName).collect(Collectors.toSet()));
            vet.setSpecialties(vetSpecialties);
        }
        vetRepository.save(vet);
        response.send(ResponseEntity.status(201).body(vetMapper.toVetDto(vet)));
    }

    public void updateVet(
            @PathVariable(name = "vetId") Integer vetId,
            @Valid @RequestBody VetDto vetDto,
            VetRepository vetRepository,
            SpecialtyRepository specialtyRepository,
            VetMapper vetMapper,
            SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<VetDto>> response) {
        Vet currentVet = findById(() -> vetRepository.findById(vetId));
        if (currentVet == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        currentVet.setFirstName(vetDto.getFirstName());
        currentVet.setLastName(vetDto.getLastName());
        currentVet.clearSpecialties();
        Collection<SpecialtyDto> specialtyDtos = vetDto.getSpecialties();
        if (specialtyDtos != null) {
            for (Specialty spec : specialtyMapper.toSpecialtys(specialtyDtos)) {
                currentVet.addSpecialty(spec);
            }
        }
        if (currentVet.getNrOfSpecialties() > 0) {
            List<Specialty> vetSpecialties = specialtyRepository.findSpecialtiesByNameIn(
                currentVet.getSpecialties().stream().map(Specialty::getName).collect(Collectors.toSet()));
            currentVet.setSpecialties(vetSpecialties);
        }
        vetRepository.save(currentVet);
        response.send(ResponseEntity.status(204).body(vetMapper.toVetDto(currentVet)));
    }

    public void deleteVet(
            @PathVariable(name = "vetId") Integer vetId,
            VetRepository vetRepository,
            ObjectResponse<ResponseEntity<VetDto>> response) {
        Vet vet = findById(() -> vetRepository.findById(vetId));
        if (vet == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        vetRepository.delete(vet);
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
