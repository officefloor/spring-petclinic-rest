package org.springframework.samples.petclinic.rest.function.visit;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadVisit} runs, so an invalid body is a 400 even when the visit does not exist.
 */
@Validated
public class ValidateVisitFields {

    public void service(@Valid @RequestBody VisitFieldsDto request, Out<VisitFieldsDto> validated) {
        validated.set(request);
    }
}
