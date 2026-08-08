package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create request whose supplied {@code registrationDate} is later than the
 * server's current date; a registration date may not lie in the future.
 *
 * <p>Runs before {@link DefaultOwnerRegistrationDate} so it inspects the date exactly
 * as supplied, before any defaulting or business-day roll-forward. A request that omits
 * the field passes through untouched (there is nothing to reject). When the supplied
 * date is after today it throws a checked {@link FutureRegistrationDateException}, which
 * the escalation handler turns into a 400 Bad Request.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val Owner owner) throws FutureRegistrationDateException {
        LocalDate supplied = owner.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                    "The registration date may not be later than the current date");
        }
    }
}
