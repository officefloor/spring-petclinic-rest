package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates the optional owner {@code registrationDate}. The date is optional, so a null
 * value is left untouched; when supplied it must not be later than the server's current
 * date (otherwise a 400 via {@link FutureRegistrationDateException}). Runs before
 * {@link ResolveRegistrationDate} so a future date is rejected before it is rolled forward
 * to a business day or counted against the daily limit.
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
