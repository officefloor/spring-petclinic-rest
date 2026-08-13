package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates the owner's registrationDate WHEN PRESENT, reading the published request body. An
 * absent date is accepted (it defaults to the server date later, see
 * {@link DefaultOwnerRegistrationDate}). A supplied date must not be later than the server's current
 * date; a future date is rejected with 400 via {@link FutureRegistrationDateException}.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }
}
