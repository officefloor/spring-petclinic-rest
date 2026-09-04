package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create request whose supplied {@code registrationDate} is later than the server's
 * current date, so an owner cannot be registered in the future. The date is optional; when omitted
 * it defaults to the server date (see {@link ApplyRegistrationDate}), so no check is made then.
 */
public class ValidateRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                    "registrationDate " + supplied + " must not be later than the server date");
        }
    }
}
