package org.springframework.samples.petclinic.rest.function.specialty;

import net.officefloor.plugin.variable.Val;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;

public class RespondWithSpecialty {

    public void service(@Val Specialty specialty, SpecialtyMapper specialtyMapper, ObjectResponse<SpecialtyDto> response) {
        response.send(specialtyMapper.toSpecialtyDto(specialty));
    }
}
