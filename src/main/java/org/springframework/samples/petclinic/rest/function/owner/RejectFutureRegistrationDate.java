package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidRegistrationDateException;

/**
 * Rejects creating an owner whose registration date is later than the server date. The
 * effective registration date — supplied in the request or defaulted to the server date
 * by {@link BuildOwner} — must not be in the future. Runs before
 * {@link NormalizeOwnerRegistrationDate}, so the check is on the supplied date rather
 * than a weekend-adjusted one. A future date escalates as a 400 via
 * {@link InvalidRegistrationDateException}.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val Owner owner) throws InvalidRegistrationDateException {
        LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new InvalidRegistrationDateException(
                    "Registration date '" + registrationDate
                            + "' is later than the server date");
        }
    }
}
