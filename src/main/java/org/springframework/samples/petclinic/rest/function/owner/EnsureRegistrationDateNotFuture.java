package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a supplied {@code registrationDate} later than the server's current date — a request
 * cannot backdate an owner into the future. The date is validated only WHEN PRESENT: a request
 * that omits it defaults to the server date (see {@link ResolveRegistrationDate}) and always
 * passes. A future date is rejected 400 via {@link FutureRegistrationDateException}.
 */
public class EnsureRegistrationDateNotFuture {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }
}
