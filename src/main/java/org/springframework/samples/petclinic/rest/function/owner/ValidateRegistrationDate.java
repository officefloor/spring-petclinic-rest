package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

import java.time.LocalDate;

/**
 * Rejects a request whose supplied registration date is later than the current server date.
 * A registration date is optional: an absent date passes unchanged and is defaulted to the
 * server date later by {@link DefaultRegistrationDate}. The check is against the supplied
 * value, before any business-day roll, so a future date cannot slip through.
 */
public class ValidateRegistrationDate {

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
