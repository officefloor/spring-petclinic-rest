package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Runs in the create-owner pipeline before {@link DefaultOwnerRegistrationDate}. Rejects a request
 * that supplies a registrationDate later than the server's current date with a 400, before the date
 * is defaulted or rolled off a weekend to a business day. An absent registrationDate (defaulted to
 * the server date later) and a supplied date on or before today are accepted.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val Owner owner) throws FutureRegistrationDateException {
        LocalDate supplied = owner.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }
}
