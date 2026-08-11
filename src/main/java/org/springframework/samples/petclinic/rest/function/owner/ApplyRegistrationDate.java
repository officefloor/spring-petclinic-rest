package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Sets an owner's effective registration date: the date supplied in the request, or the server's
 * current date when none was supplied, rolled forward to the next business day when it lands on a
 * weekend. Mutates the built owner in place, so every value derived from the registration date
 * (such as the membership number's year segment) uses the adjusted date.
 */
public class ApplyRegistrationDate {

    public void service(@Val Owner owner) {
        owner.setRegistrationDate(RegistrationDate.effective(owner.getRegistrationDate()));
    }
}
