package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Step of {@code POST /api/owners} that rejects a create whose supplied {@code registrationDate} is
 * later than the server's current date, throwing {@link FutureRegistrationDateException} (handled as
 * 400). A registration cannot be dated in the future. The check uses the date exactly as supplied
 * (before any business-day roll); registration date is optional, so an absent date is accepted and
 * later defaults to today.
 */
public class RequireRegistrationDateNotFuture {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                    "registrationDate " + supplied + " must not be later than the current date");
        }
    }
}
