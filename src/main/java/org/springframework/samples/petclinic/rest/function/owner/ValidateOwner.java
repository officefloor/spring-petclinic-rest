package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws InvalidEmailException {
        // Email is optional; when present it must be syntactically valid and is stored lower-cased.
        request.setEmail(OwnerEmails.normalize(request.getEmail()));
        validated.set(request);
    }
}
