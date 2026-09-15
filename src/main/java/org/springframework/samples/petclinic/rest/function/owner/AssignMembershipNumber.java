package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a newly built owner its {@code membershipNumber}, formatted
 * {@code <customerCode>-M<YY>} where YY is the last two digits of the owner's
 * registration date year (e.g. {@code SMI-0007-M26}). Runs after
 * {@link AssignCustomerCode} and {@link DefaultRegistrationDate} so both inputs are
 * set, and before {@link SaveOwner}, mutating the built owner in place so the number
 * is stored and returned with the owner.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
