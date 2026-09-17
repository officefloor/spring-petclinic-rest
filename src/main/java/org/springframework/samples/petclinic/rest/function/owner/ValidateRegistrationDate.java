package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates a supplied registrationDate <em>only when present</em>: it may not be later than the
 * server's current date. A missing date is allowed and later defaults to the server date (see
 * {@link ResolveRegistrationDate}). Judges the raw supplied date, before it is rolled onto a
 * business day, so the check reflects exactly what the request asked for. A future date is a 400 via
 * {@link FutureRegistrationDateException}.
 */
public class ValidateRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }
}
