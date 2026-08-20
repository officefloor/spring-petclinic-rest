package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber} formatted {@code <customerCode>-M<YY>}, where YY is
 * the last two digits of the registrationDate year (e.g. {@code SMI-0007-M26}). Runs after the
 * customer code is assigned and the registration date is set.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
