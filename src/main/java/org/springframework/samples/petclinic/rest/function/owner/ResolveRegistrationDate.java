package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Resolves an owner's effective registration date on creation and stores it on the built
 * owner in place. A supplied date is kept, otherwise the server's current date is used; the
 * effective date is then rolled forward onto a business day (see {@link RegistrationDates}),
 * so a weekend date becomes the following Monday. Later steps (customer code, membership
 * number) read the stored, adjusted date.
 */
public class ResolveRegistrationDate {

    public void service(@Val Owner owner) {
        owner.setRegistrationDate(RegistrationDates.effective(owner.getRegistrationDate()));
    }
}
