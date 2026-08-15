package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Step of {@code POST /api/owners} that runs after {@link ValidateOwnerFields} has published the
 * request and before {@link ResolveRegistrationDate}. Rejects the request when the supplied
 * {@code registrationDate} is later than the server's current date, so a future date is a 400 Bad
 * Request rather than being accepted or rolled forward to a business day. A request without a
 * {@code registrationDate} (the date defaults to today) is always allowed.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }
}
