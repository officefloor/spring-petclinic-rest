package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a new owner whose supplied {@code registrationDate} is later than the server's current date:
 * an owner cannot be registered in the future, so the request is answered with a 400 Bad Request. A
 * request that omits the date is accepted (it defaults to the server date in {@link BuildOwner}). Runs
 * among the create-time validations, before {@link BuildOwner} stores the (possibly adjusted) date.
 */
public class ValidateRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        if (RegistrationDates.isFuture(request.getRegistrationDate())) {
            throw new FutureRegistrationDateException(request.getRegistrationDate());
        }
    }
}
