package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request with a 400 (via {@code FutureRegistrationDateExceptionHandler}) when
 * the supplied {@code registrationDate} is later than the server's current date. Runs before
 * {@link DefaultOwnerRegistrationDate}, so it validates the value the client actually supplied; an
 * absent (null) date is left for that step to default and is never rejected here.
 */
public class RequireRegistrationDateNotFuture {

    public void service(@Val OwnerFieldsDto owner) throws FutureRegistrationDateException {
        LocalDate date = owner.getRegistrationDate();
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(date);
        }
    }
}
