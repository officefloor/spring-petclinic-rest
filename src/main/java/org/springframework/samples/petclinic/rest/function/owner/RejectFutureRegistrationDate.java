package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects 400 when the request supplied a {@code registrationDate} later than the server date.
 * The built date has been rolled to its next business day, so it is compared against today rolled
 * the same way — leaving a same-day (or absent) registration accepted while a future date is not.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val Owner owner) throws FutureRegistrationDateException {
        if (owner.getRegistrationDate().isAfter(BusinessDay.nextBusinessDay(LocalDate.now()))) {
            throw new FutureRegistrationDateException();
        }
    }
}
