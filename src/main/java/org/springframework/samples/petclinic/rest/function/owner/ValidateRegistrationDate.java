package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates an optional registration date on an owner create request. The date is not
 * required: a null value is left untouched and later defaulted to the server's current date
 * (see {@link RegistrationDates}). When supplied it must not be later than the server date,
 * otherwise the request is rejected with {@link FutureRegistrationDateException} (400). Runs
 * before {@link BuildOwner} maps the body to an entity.
 */
public class ValidateRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied == null) {
            return;
        }
        LocalDate serverDate = LocalDate.now();
        if (supplied.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(supplied, serverDate);
        }
    }
}
