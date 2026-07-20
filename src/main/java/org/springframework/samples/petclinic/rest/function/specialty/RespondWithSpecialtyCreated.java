package org.springframework.samples.petclinic.rest.function.specialty;

import java.net.URI;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

public class RespondWithSpecialtyCreated {

    public void service(@Val Specialty specialty, SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<SpecialtyDto>> response) {
        SpecialtyDto dto = specialtyMapper.toSpecialtyDto(specialty);
        response.send(ResponseEntity.created(URI.create("/api/specialties/" + specialty.getId())).body(dto));
    }
}
