package org.springframework.samples.petclinic.rest.function.vet;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.VetDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadVet} runs, so an invalid body is a 400 even when the vet does not exist.
 */
@Validated
public class ValidateVet {

    public void service(@Valid @RequestBody VetDto request, Out<VetDto> validated) {
        validated.set(request);
    }
}
