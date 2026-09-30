package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a supplied registration date that is later than the server's current date: registration
 * cannot be dated in the future. The date is optional on create (it defaults to today when absent),
 * so an absent date is skipped entirely. A future date is rejected 400 via
 * {@link FutureRegistrationDateException}.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                "Registration date " + registrationDate + " must not be later than the current date");
        }
    }
}
