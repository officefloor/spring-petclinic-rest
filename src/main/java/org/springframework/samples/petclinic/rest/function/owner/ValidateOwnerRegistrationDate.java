package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates an owner request's optional registration date: an absent date is left for
 * {@link DefaultOwnerRegistrationDate} to stamp. A supplied date later than the server's
 * current date is rejected with a 400. Nothing is mutated.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate == null) {
            return;
        }
        LocalDate serverDate = LocalDate.now();
        if (registrationDate.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(registrationDate, serverDate);
        }
    }
}
