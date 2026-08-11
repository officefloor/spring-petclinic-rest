package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'}:
 * customerCode is the value assigned by {@link AssignCustomerCode} and YY is the last two
 * digits of the owner's {@code registrationDate} year, zero-padded (e.g.
 * {@code 'SYD-SMI-0007-M26'}). Runs after {@link AssignCustomerCode} (so customerCode exists)
 * and mutates the built {@link Owner} in place.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
