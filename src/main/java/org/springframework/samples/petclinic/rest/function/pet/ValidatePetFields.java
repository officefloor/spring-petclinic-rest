package org.springframework.samples.petclinic.rest.function.pet;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class ValidatePetFields {

    public void service(@Valid @RequestBody PetFieldsDto request, Out<PetFieldsDto> validated) {
        validated.set(request);
    }
}
