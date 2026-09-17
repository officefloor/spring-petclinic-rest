package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request whose supplied {@link Owner#getRegistrationDate()
 * registrationDate} is later than the server's current date, responding 400 via
 * {@link FutureRegistrationDateException}. Runs before the registration date is resolved,
 * so it validates the value as supplied; a request that omits the date (defaulted later to
 * the server date) is left untouched.
 */
public class RequireRegistrationDateNotFuture {

    public void service(@Val Owner owner) throws FutureRegistrationDateException {
        LocalDate supplied = owner.getRegistrationDate();
        LocalDate serverDate = LocalDate.now();
        if (supplied != null && supplied.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(supplied, serverDate);
        }
    }
}
