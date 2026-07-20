package org.springframework.samples.petclinic.rest.function.specialty;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildSpecialty {

    public void service(@Valid @RequestBody SpecialtyDto request, SpecialtyMapper specialtyMapper, Out<Specialty> built) {
        built.set(specialtyMapper.toSpecialty(request));
    }
}
