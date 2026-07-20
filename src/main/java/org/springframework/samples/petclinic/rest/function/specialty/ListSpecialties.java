package org.springframework.samples.petclinic.rest.function.specialty;

import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

public class ListSpecialties {

    public void service(SpecialtyRepository specialtyRepository, SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<List<SpecialtyDto>>> response) {
        List<SpecialtyDto> specialties = List.copyOf(specialtyMapper.toSpecialtyDtos(specialtyRepository.findAll()));
        if (specialties.isEmpty()) {
            response.send(ResponseEntity.notFound().build());
            return;
        }
        response.send(ResponseEntity.ok(specialties));
    }
}
