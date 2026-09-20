package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Rolls a new owner's effective registration date onto a business day: a Saturday or Sunday
 * moves forward to the following Monday. Runs after {@link DefaultRegistrationDate}, so it
 * adjusts a client-supplied weekend date and the server-defaulted date alike. Later steps —
 * the daily create-limit and the membership number's year segment — then see the adjusted date.
 */
public class RollRegistrationDateToBusinessDay {

    public void service(@Val Owner owner) {
        owner.setRegistrationDate(BusinessDays.rollForward(owner.getRegistrationDate()));
    }
}
