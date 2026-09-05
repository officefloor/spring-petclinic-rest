package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request whose supplied {@code registrationDate} is later than the
 * server's current date — a registration cannot be dated in the future. The field is optional:
 * an absent registrationDate passes untouched (the server date is used downstream). A supplied
 * date on or before today is accepted; anything later is rejected 400 via
 * {@link FutureRegistrationDateException}.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                    "Registration date " + registrationDate + " must not be later than the server date");
        }
    }
}
