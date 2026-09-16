package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request that supplies a registration date later than the server's
 * current date, responding 400. Runs before {@link ResolveOwnerRegistrationDate} so the raw
 * supplied date is checked before any business-day roll-forward adjusts it. A request that
 * omits the date is accepted unchanged.
 */
public class RejectFutureRegistrationDate {

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
