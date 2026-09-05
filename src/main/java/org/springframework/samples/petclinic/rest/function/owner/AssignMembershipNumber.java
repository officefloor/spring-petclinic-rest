package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code membershipNumber} formatted {@code <customerCode>-M<YY>}, where YY is
 * the last two digits of the owner's {@code registrationDate} year (e.g. {@code SMI-0007-M26}).
 * Runs after {@link AssignCustomerCode} and after the registration date has been defaulted.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
