package org.springframework.samples.petclinic.rest.function.pettype;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadPetType} runs, so an invalid body is a 400 even when the pet type does not exist.
 */
@Validated
public class ValidatePetType {

    public void service(@Valid @RequestBody PetTypeDto request, Out<PetTypeDto> validated) {
        validated.set(request);
    }
}
