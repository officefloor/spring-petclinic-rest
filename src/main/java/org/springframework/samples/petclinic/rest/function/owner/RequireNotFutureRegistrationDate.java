package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create request whose supplied {@code registrationDate} is later than the
 * server's current date, before {@link BuildOwner} runs. Reported as a 400 by
 * {@link org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateExceptionHandler}.
 *
 * <p>A request that omits the date is left for {@link BuildOwner} to default to the
 * server date, so no guard is needed there.
 */
public class RequireNotFutureRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate today = LocalDate.now();
        if (supplied != null && supplied.isAfter(today)) {
            throw new FutureRegistrationDateException(supplied, today);
        }
    }
}
