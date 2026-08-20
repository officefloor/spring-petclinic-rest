package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates the owner's registrationDate WHEN PRESENT. An absent registrationDate is
 * accepted (it defaults to the server date when the owner is built). When supplied it must
 * not be later than the server's current date; a future date is rejected with a 400 via
 * {@link FutureRegistrationDateException}. Runs before {@code BuildOwner}, which would
 * otherwise roll the supplied date forward to a business day.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate == null) {
            return; // optional when absent; defaults to server date at build time
        }
        LocalDate serverDate = LocalDate.now();
        if (registrationDate.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(registrationDate, serverDate);
        }
    }
}
