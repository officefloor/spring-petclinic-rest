package org.springframework.samples.petclinic.rest.endpoint;

import jakarta.validation.Valid;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Validated
public class SpecialtyEndpoint {

    public void listSpecialties(
            SpecialtyRepository specialtyRepository,
            SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<List<SpecialtyDto>>> response) {
        Collection<Specialty> specialties = specialtyRepository.findAll();
        if (specialties.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(new ArrayList<>(specialtyMapper.toSpecialtyDtos(specialties))));
    }

    public void getSpecialty(
            @PathVariable(name = "specialtyId") Integer specialtyId,
            SpecialtyRepository specialtyRepository,
            SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<SpecialtyDto>> response) {
        Specialty specialty = findById(() -> specialtyRepository.findById(specialtyId));
        if (specialty == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(specialtyMapper.toSpecialtyDto(specialty)));
    }

    public void addSpecialty(
            @Valid @RequestBody SpecialtyDto specialtyDto,
            SpecialtyRepository specialtyRepository,
            SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<SpecialtyDto>> response) {
        Specialty specialty = specialtyMapper.toSpecialty(specialtyDto);
        specialtyRepository.save(specialty);
        response.send(ResponseEntity.status(201).body(specialtyMapper.toSpecialtyDto(specialty)));
    }

    public void updateSpecialty(
            @PathVariable(name = "specialtyId") Integer specialtyId,
            @Valid @RequestBody SpecialtyDto specialtyDto,
            SpecialtyRepository specialtyRepository,
            SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<SpecialtyDto>> response) {
        Specialty currentSpecialty = findById(() -> specialtyRepository.findById(specialtyId));
        if (currentSpecialty == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        currentSpecialty.setName(specialtyDto.getName());
        specialtyRepository.save(currentSpecialty);
        response.send(ResponseEntity.status(204).body(specialtyMapper.toSpecialtyDto(currentSpecialty)));
    }

    public void deleteSpecialty(
            @PathVariable(name = "specialtyId") Integer specialtyId,
            SpecialtyRepository specialtyRepository,
            ObjectResponse<ResponseEntity<SpecialtyDto>> response) {
        Specialty specialty = findById(() -> specialtyRepository.findById(specialtyId));
        if (specialty == null) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        specialtyRepository.delete(specialty);
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
