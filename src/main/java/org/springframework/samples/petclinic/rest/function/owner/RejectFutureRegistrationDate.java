package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request that supplies a {@code registrationDate} later than the
 * server's current date.
 *
 * <p>The registration date is optional (an absent date defaults to the server date in
 * {@link ResolveRegistrationDate}), so only a supplied date is checked. A supplied date
 * on or before today passes untouched; a future date is rejected with a
 * {@link FutureRegistrationDateException} (400). Runs before {@link ResolveRegistrationDate}
 * so a future date never reaches the business-day roll or any downstream step.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                    "registrationDate " + supplied + " must not be later than the server date");
        }
    }
}
