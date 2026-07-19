package org.springframework.samples.petclinic.rest.function.specialty;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadSpecialty} runs, so an invalid body is a 400 even when the specialty does not exist.
 */
@Validated
public class ValidateSpecialty {

    public void service(@Valid @RequestBody SpecialtyDto request, Out<SpecialtyDto> validated) {
        validated.set(request);
    }
}
