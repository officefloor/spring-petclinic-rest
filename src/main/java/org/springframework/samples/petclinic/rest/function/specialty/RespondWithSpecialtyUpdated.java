package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

public class RespondWithSpecialtyUpdated {

    public void service(@Val Specialty specialty, SpecialtyMapper specialtyMapper,
            ObjectResponse<ResponseEntity<SpecialtyDto>> response) {
        response.send(ResponseEntity.status(HttpStatus.NO_CONTENT).body(specialtyMapper.toSpecialtyDto(specialty)));
    }
}
