package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 *
 * <p>Email validity and normalization go through {@link OwnerEmail}, the single authority shared with the
 * create pipeline, rather than a bean-validation {@code @Email} constraint, so both pipelines agree.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws InvalidEmailException {
        String email = OwnerEmail.normalize(request.getEmail());
        if (email != null && !OwnerEmail.isValid(email)) {
            throw new InvalidEmailException("Email is not a valid address: " + request.getEmail());
        }
        request.setEmail(email);
        validated.set(request);
    }
}
