package org.springframework.samples.petclinic.rest.function.pettype;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.PetTypeFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class BuildPetType {

    public void service(@Valid @RequestBody PetTypeFieldsDto request, PetTypeMapper petTypeMapper, Out<PetType> built) {
        built.set(petTypeMapper.toPetType(request));
    }
}
