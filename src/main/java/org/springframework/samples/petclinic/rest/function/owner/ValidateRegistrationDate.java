package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request whose supplied registration date is later than the server's
 * current date. A missing date is left for {@link ResolveRegistrationDate} to default. Runs before
 * the date is resolved so a future date is a 400, not silently rolled forward to a business day.
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
