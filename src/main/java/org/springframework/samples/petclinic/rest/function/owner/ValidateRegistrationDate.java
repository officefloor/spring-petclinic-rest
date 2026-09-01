package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Reads and validates the request body first: a supplied registrationDate later than the server's
 * current date is rejected as 400, before any owner is built or persisted. This step owns the single
 * {@code @RequestBody} binding and republishes the body as a variable for {@link BuildOwner} to read
 * via {@code @Val}.
 */
@Validated
public class ValidateRegistrationDate {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> body)
            throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
        body.set(request);
    }
}
