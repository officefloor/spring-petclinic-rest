package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsValidationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 *
 * <p>Also normalizes the optional email (syntactic check + lower-casing) so update and create treat
 * email identically; a present-but-invalid email is a 400.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsValidationException {
        List<String> errors = new ArrayList<>();
        OwnerEmail.normalize(request, errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsValidationException(errors);
        }
        validated.set(request);
    }
}
