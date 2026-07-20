package org.springframework.samples.petclinic.rest.function.pet;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
public class ValidatePetDto {

    public void service(@Valid @RequestBody PetDto request, Out<PetDto> validated) {
        validated.set(request);
    }
}
